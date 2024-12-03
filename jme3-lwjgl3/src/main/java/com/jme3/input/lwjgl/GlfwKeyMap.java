/*
 * Copyright (c) 2009-2021 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jme3.input.lwjgl;

import static org.lwjgl.glfw.GLFW.*;
import static com.jme3.input.KeyInput.*;

public class GlfwKeyMap {

    private static final int[] GLFW_TO_JME_KEY_MAP = new int[GLFW_KEY_LAST + 1];

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private GlfwKeyMap() {
    }

    private static void reg(final int jmeKey, final int glfwKey) {
        GLFW_TO_JME_KEY_MAP[glfwKey] = jmeKey;
    }

    private static void registerKeys() {
        int[][] keyMappings = {
            {KEY_ESCAPE, GLFW_KEY_ESCAPE},
            {KEY_1, GLFW_KEY_1},
            {KEY_2, GLFW_KEY_2},
            {KEY_3, GLFW_KEY_3},
            {KEY_4, GLFW_KEY_4},
            {KEY_5, GLFW_KEY_5},
            {KEY_6, GLFW_KEY_6},
            {KEY_7, GLFW_KEY_7},
            {KEY_8, GLFW_KEY_8},
            {KEY_9, GLFW_KEY_9},
            {KEY_0, GLFW_KEY_0},
            {KEY_MINUS, GLFW_KEY_MINUS},
            {KEY_EQUALS, GLFW_KEY_EQUAL},
            {KEY_BACK, GLFW_KEY_BACKSPACE},
            {KEY_TAB, GLFW_KEY_TAB},
            {KEY_Q, GLFW_KEY_Q},
            {KEY_W, GLFW_KEY_W},
            {KEY_E, GLFW_KEY_E},
            {KEY_R, GLFW_KEY_R},
            {KEY_T, GLFW_KEY_T},
            {KEY_Y, GLFW_KEY_Y},
            {KEY_U, GLFW_KEY_U},
            {KEY_I, GLFW_KEY_I},
            {KEY_O, GLFW_KEY_O},
            {KEY_P, GLFW_KEY_P},
            {KEY_LBRACKET, GLFW_KEY_LEFT_BRACKET},
            {KEY_RBRACKET, GLFW_KEY_RIGHT_BRACKET},
            {KEY_RETURN, GLFW_KEY_ENTER},
            {KEY_LCONTROL, GLFW_KEY_LEFT_CONTROL},
            {KEY_A, GLFW_KEY_A},
            {KEY_S, GLFW_KEY_S},
            {KEY_D, GLFW_KEY_D},
            {KEY_F, GLFW_KEY_F},
            {KEY_G, GLFW_KEY_G},
            {KEY_H, GLFW_KEY_H},
            {KEY_J, GLFW_KEY_J},
            {KEY_K, GLFW_KEY_K},
            {KEY_L, GLFW_KEY_L},
            {KEY_SEMICOLON, GLFW_KEY_SEMICOLON},
            {KEY_APOSTROPHE, GLFW_KEY_APOSTROPHE},
            {KEY_GRAVE, GLFW_KEY_GRAVE_ACCENT},
            {KEY_LSHIFT, GLFW_KEY_LEFT_SHIFT},
            {KEY_BACKSLASH, GLFW_KEY_BACKSLASH},
            {KEY_Z, GLFW_KEY_Z},
            {KEY_X, GLFW_KEY_X},
            {KEY_C, GLFW_KEY_C},
            {KEY_V, GLFW_KEY_V},
            {KEY_B, GLFW_KEY_B},
            {KEY_N, GLFW_KEY_N},
            {KEY_M, GLFW_KEY_M},
            {KEY_COMMA, GLFW_KEY_COMMA},
            {KEY_PERIOD, GLFW_KEY_PERIOD},
            {KEY_SLASH, GLFW_KEY_SLASH},
            {KEY_RSHIFT, GLFW_KEY_RIGHT_SHIFT},
            {KEY_MULTIPLY, GLFW_KEY_KP_MULTIPLY},
            {KEY_LMENU, GLFW_KEY_LEFT_ALT},
            {KEY_SPACE, GLFW_KEY_SPACE},
            {KEY_CAPITAL, GLFW_KEY_CAPS_LOCK},
            {KEY_F1, GLFW_KEY_F1},
            {KEY_F2, GLFW_KEY_F2},
            {KEY_F3, GLFW_KEY_F3},
            {KEY_F4, GLFW_KEY_F4},
            {KEY_F5, GLFW_KEY_F5},
            {KEY_F6, GLFW_KEY_F6},
            {KEY_F7, GLFW_KEY_F7},
            {KEY_F8, GLFW_KEY_F8},
            {KEY_F9, GLFW_KEY_F9},
            {KEY_F10, GLFW_KEY_F10},
            {KEY_NUMLOCK, GLFW_KEY_NUM_LOCK},
            {KEY_SCROLL, GLFW_KEY_SCROLL_LOCK},
            {KEY_NUMPAD7, GLFW_KEY_KP_7},
            {KEY_NUMPAD8, GLFW_KEY_KP_8},
            {KEY_NUMPAD9, GLFW_KEY_KP_9},
            {KEY_SUBTRACT, GLFW_KEY_KP_SUBTRACT},
            {KEY_NUMPAD4, GLFW_KEY_KP_4},
            {KEY_NUMPAD5, GLFW_KEY_KP_5},
            {KEY_NUMPAD6, GLFW_KEY_KP_6},
            {KEY_ADD, GLFW_KEY_KP_ADD},
            {KEY_NUMPAD1, GLFW_KEY_KP_1},
            {KEY_NUMPAD2, GLFW_KEY_KP_2},
            {KEY_NUMPAD3, GLFW_KEY_KP_3},
            {KEY_NUMPAD0, GLFW_KEY_KP_0},
            {KEY_DECIMAL, GLFW_KEY_KP_DECIMAL},
            {KEY_F11, GLFW_KEY_F11},
            {KEY_F12, GLFW_KEY_F12},
            {KEY_F13, GLFW_KEY_F13},
            {KEY_F14, GLFW_KEY_F14},
            {KEY_F15, GLFW_KEY_F15},
            {KEY_NUMPADENTER, GLFW_KEY_KP_ENTER},
            {KEY_RCONTROL, GLFW_KEY_RIGHT_CONTROL},
            {KEY_DIVIDE, GLFW_KEY_KP_DIVIDE},
            {KEY_SYSRQ, GLFW_KEY_PRINT_SCREEN},
            {KEY_RMENU, GLFW_KEY_RIGHT_ALT},
            {KEY_PAUSE, GLFW_KEY_PAUSE},
            {KEY_HOME, GLFW_KEY_HOME},
            {KEY_UP, GLFW_KEY_UP},
            {KEY_PRIOR, GLFW_KEY_PAGE_UP},
            {KEY_LEFT, GLFW_KEY_LEFT},
            {KEY_RIGHT, GLFW_KEY_RIGHT},
            {KEY_END, GLFW_KEY_END},
            {KEY_DOWN, GLFW_KEY_DOWN},
            {KEY_NEXT, GLFW_KEY_PAGE_DOWN},
            {KEY_INSERT, GLFW_KEY_INSERT},
            {KEY_DELETE, GLFW_KEY_DELETE},
            {KEY_LMETA, GLFW_KEY_LEFT_SUPER},
            {KEY_RMETA, GLFW_KEY_RIGHT_SUPER}
        };

        for (int[] mapping : keyMappings) {
            reg(mapping[0], mapping[1]);
        }
    }

    static {
        registerKeys();
    }

    /**
     * Returns the jme keycode that matches the specified glfw keycode
     * @param glfwKey the glfw keycode
     */
    public static int toJmeKeyCode(final int glfwKey) {
        return GLFW_TO_JME_KEY_MAP[glfwKey];
    }

    /**
     * Returns the glfw keycode that matches the specified jme keycode or
     * GLFW_KEY_UNKNOWN if there isn't any match.
     * 
     * @param jmeKey the jme keycode
     */
    public static int fromJmeKeyCode(final int jmeKey) {
        for (int i = 0; i < GLFW_TO_JME_KEY_MAP.length; i++) {
            if (GLFW_TO_JME_KEY_MAP[i] == jmeKey) return i;
        }
        return GLFW_KEY_UNKNOWN;
    }
}
