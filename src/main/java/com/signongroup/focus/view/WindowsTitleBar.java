package com.signongroup.focus.view;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;
import javafx.stage.Stage;

/**
 * Utility to style the native Windows 11 title bar using JNA.
 */
public final class WindowsTitleBar {

    private static final int DWMWA_CAPTION_COLOR = 35;
    private static final int DWMWA_TEXT_COLOR = 36;

    private WindowsTitleBar() {
        // Utility class
    }

    public interface DwmApi extends StdCallLibrary {
        DwmApi INSTANCE = Native.load("dwmapi", DwmApi.class, W32APIOptions.DEFAULT_OPTIONS);

        /**
         * Sets the value of Desktop Window Manager (DWM) non-client rendering attributes for a window.
         *
         * @param hwnd The handle to the window for which the attribute value is to be set.
         * @param dwAttribute A flag describing which value to set.
         * @param pvAttribute A pointer to an object containing the attribute value to set.
         * @param cbAttribute The size, in bytes, of the attribute value being set via the pvAttribute parameter.
         * @return If the function succeeds, it returns S_OK.
         */
        @SuppressWarnings("checkstyle:MethodName")
        int DwmSetWindowAttribute(HWND hwnd, int dwAttribute, IntByReference pvAttribute, int cbAttribute);
    }

    /**
     * Applies custom background and text colors to the native Windows title bar.
     * This relies on DWM APIs introduced in Windows 11.
     *
     * @param stage The primary JavaFX stage.
     */
    public static void applyCustomColors(Stage stage) {
        if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
            return;
        }

        try {
            // Retrieve the HWND of the JavaFX Stage using User32
            String title = stage.getTitle();
            if (title == null || title.isEmpty()) {
                return;
            }

            HWND hwnd = User32.INSTANCE.FindWindow(null, title);
            if (hwnd == null) {
                return;
            }

            // Set Background Color: #0e0e0e (Format: 0x00BBGGRR)
            int bgColor = 0x000e0e0e;
            IntByReference bgRef = new IntByReference(bgColor);
            DwmApi.INSTANCE.DwmSetWindowAttribute(hwnd, DWMWA_CAPTION_COLOR, bgRef, 4);

            // Set Text/Icon Color: #ff8f70 (Format: 0x00BBGGRR -> 0x00708fff)
            int textColor = 0x00708fff;
            IntByReference textRef = new IntByReference(textColor);
            DwmApi.INSTANCE.DwmSetWindowAttribute(hwnd, DWMWA_TEXT_COLOR, textRef, 4);

        } catch (LinkageError e) {
            // Ignore if native library cannot be loaded or is unsupported
        }
    }
}
