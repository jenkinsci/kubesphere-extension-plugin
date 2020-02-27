package io.jenkins.kubesphere.plugins.event;

import hudson.model.AbstractBuild;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.tokenmacro.TokenMacro;

import java.util.Arrays;

public class Utils {
    private Utils () {
    }

    /**
     * Determines if any of Strings specified is either null or empty.
     * @param strings - Strings to check for empty (whitespace is trimmed) or null.
     * @return True if any string is empty
     */
    @SuppressWarnings( "MethodWithMultipleReturnPoints" )
    public static boolean isEmpty( String ... strings ) {
        if (( strings == null ) || ( strings.length < 1 )) {
            return true;
        }

        for ( String s : strings ) {
            if (( s == null ) || ( s.trim().length() < 1 )) {
                return true;
            }
        }
        return false;
    }


    /**
     * Verifies neither of Strings specified is null or empty.
     * @param strings Strings to check for empty (whitespace is trimmed) or null.
     * @throws java.lang.IllegalArgumentException Throws this exception if any string is empty.
     */
    @SuppressWarnings( "ReturnOfNull" )
    public static void verifyNotEmpty( String ... strings ) {
        if ( isEmpty( strings )) {
            throw new IllegalArgumentException( String.format(
                    "Some String arguments are null or empty: %s", Arrays.toString( strings )));
        }
    }


}
