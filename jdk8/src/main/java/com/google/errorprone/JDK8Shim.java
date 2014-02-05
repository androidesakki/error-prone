package com.google.errorprone;

import com.google.errorprone.fixes.AdjustedPosition8;
import com.google.errorprone.fixes.IndexedPosition8;

import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.main.Main;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.AbstractLog;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.DiagnosticSource;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;
import com.sun.tools.javac.util.List;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.annotation.processing.Processor;
import javax.tools.JavaFileObject;

public class JDK8Shim implements JDKCompatibleShim {

  @Override
  public DiagnosticPosition getAdjustedPosition(JCTree position, int startPosAdjustment,
      int endPosAdjustment) {
    return new AdjustedPosition8(position, startPosAdjustment, endPosAdjustment);
  }

  @Override
  public DiagnosticPosition getIndexedPosition(int startPos, int endPos) {
    return new IndexedPosition8(startPos, endPos);
  }

  @Override
  public EndPosMap8 getEndPosMap(JCCompilationUnit compilationUnit) {
    return EndPosMap8.fromCompilationUnit(compilationUnit);
  }

  private static final Method ABSTRACT_LOG__GET_SOURCE;
  private static final Field DIAGNOSTIC_SOURCE__END_POS_TABLE;
  static {
    try {
      ABSTRACT_LOG__GET_SOURCE =
          AbstractLog.class.getDeclaredMethod("getSource", JavaFileObject.class);
      ABSTRACT_LOG__GET_SOURCE.setAccessible(true);

      DIAGNOSTIC_SOURCE__END_POS_TABLE =
          DiagnosticSource.class.getDeclaredField("endPosTable");
      DIAGNOSTIC_SOURCE__END_POS_TABLE.setAccessible(true);
    } catch (Exception e) {
      throw new LinkageError(e.getMessage());
    }
  }
  @Override
  public void resetEndPosMap(JavaCompiler compiler, JavaFileObject sourceFile) {
    try {
      DiagnosticSource diagnosticSource = (DiagnosticSource)
          ABSTRACT_LOG__GET_SOURCE.invoke(compiler.log, sourceFile);
      DIAGNOSTIC_SOURCE__END_POS_TABLE.set(diagnosticSource, null);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  @Override
  public int runCompile(Main main, String[] args, Context context, List<JavaFileObject> files,
      Iterable<? extends Processor> processors) {
    return main.compile(args, context, files, processors).exitCode;
  }

  @Override
  public int getJCTreeTag(JCTree node) {
    return node.getTag().ordinal();
  }
}