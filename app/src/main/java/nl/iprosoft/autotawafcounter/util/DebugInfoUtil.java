package nl.iprosoft.autotawafcounter.util;

import android.util.Log;

import java.util.Optional;


public class DebugInfoUtil {

    private static final String TAG = "DebugInfoUtil";
    public static void logExceptionDetails(Exception e) {
        if (e == null) {
            Log.e(TAG, "Exception is null, cannot log exception details.");
            return;
        }

        String className = "UnknownClass";
        String methodName = "UnknownMethod";
        int lineNumber = -1;
        String exceptionMessage = Optional.ofNullable(e.getMessage()).orElse("No message available");

        StackTraceElement[] stackTraceElements = e.getStackTrace();
        if (stackTraceElements != null && stackTraceElements.length > 0) {
            StackTraceElement element = stackTraceElements[0];
            className = Optional.ofNullable(element.getClassName()).orElse(className);
            methodName = Optional.ofNullable(element.getMethodName()).orElse(methodName);
            lineNumber = element.getLineNumber();
        }

        // Construct error details string
        String errorDetails = "ClassName=" + className +
                " | MethodName=" + methodName +
                " | LineNumber=" + lineNumber +
                " | ExceptionMessage=" + exceptionMessage;

        // Build the stack trace safely
        StringBuilder stackTrace = new StringBuilder();
        if (stackTraceElements != null) {
            for (StackTraceElement el : stackTraceElements) {
                if (el != null) {
                    stackTrace.append(el.toString()).append("\n");
                } else {
                    stackTrace.append("Null stack trace element\n");
                }
            }
        } else {
            stackTrace.append("No stack trace available");
        }

        Log.e(TAG, "Error Details: " + errorDetails);
        Log.e(TAG, "Stack Trace:\n" + stackTrace);
    }
}
