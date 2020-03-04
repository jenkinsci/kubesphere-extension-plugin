/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jenkins.kubesphere.plugins.event;

import java.util.Arrays;

public class Utils {
    private Utils() {
    }

    /**
     * Determines if any of Strings specified is either null or empty.
     *
     * @param strings - Strings to check for empty (whitespace is trimmed) or null.
     * @return True if any string is empty
     */
    @SuppressWarnings("MethodWithMultipleReturnPoints")
    public static boolean isEmpty(String... strings) {
        if ((strings == null) || (strings.length < 1)) {
            return true;
        }

        for (String s : strings) {
            if ((s == null) || (s.trim().length() < 1)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Verifies neither of Strings specified is null or empty.
     *
     * @param strings Strings to check for empty (whitespace is trimmed) or null.
     * @throws java.lang.IllegalArgumentException Throws this exception if any string is empty.
     */
    @SuppressWarnings("ReturnOfNull")
    public static void verifyNotEmpty(String... strings) {
        if (isEmpty(strings)) {
            throw new IllegalArgumentException(String.format(
                    "Some String arguments are null or empty: %s", Arrays.toString(strings)));
        }
    }


}
