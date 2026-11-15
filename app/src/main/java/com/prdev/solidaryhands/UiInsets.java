package com.prdev.solidaryhands;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

/**
 * Dibuja el fondo de borde a borde y protege solamente la cabecera y las
 * acciones inferiores de cada pantalla.
 */
public final class UiInsets {

    private UiInsets() {}

    @SuppressWarnings("deprecation")
    public static void aplicar(
            Activity activity,
            @IdRes int vistaSuperiorId,
            @IdRes int vistaInferiorId,
            boolean iconosSuperioresOscuros,
            boolean iconosInferioresOscuros
    ) {
        WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), false);
        activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
        activity.getWindow().setNavigationBarColor(Color.TRANSPARENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activity.getWindow().setNavigationBarContrastEnforced(false);
        }

        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(
                activity.getWindow(), activity.getWindow().getDecorView()
        );
        controller.setAppearanceLightStatusBars(iconosSuperioresOscuros);
        controller.setAppearanceLightNavigationBars(iconosInferioresOscuros);

        View superior = activity.findViewById(vistaSuperiorId);
        View inferior = activity.findViewById(vistaInferiorId);
        View content = activity.findViewById(android.R.id.content);

        int topLeft = superior.getPaddingLeft();
        int topTop = superior.getPaddingTop();
        int topRight = superior.getPaddingRight();
        int topBottom = superior.getPaddingBottom();

        int bottomLeft = inferior.getPaddingLeft();
        int bottomTop = inferior.getPaddingTop();
        int bottomRight = inferior.getPaddingRight();
        int bottomBottom = inferior.getPaddingBottom();

        int espacio = Math.round(
                12 * activity.getResources().getDisplayMetrics().density
        );

        ViewCompat.setOnApplyWindowInsetsListener(content, (view, windowInsets) -> {
            Insets safe = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                            | WindowInsetsCompat.Type.displayCutout()
                            | WindowInsetsCompat.Type.ime()
            );

            if (superior == inferior) {
                superior.setPadding(
                        topLeft + safe.left,
                        topTop + safe.top + espacio,
                        topRight + safe.right,
                        topBottom + safe.bottom + espacio
                );
            } else {
                superior.setPadding(
                        topLeft + safe.left,
                        topTop + safe.top + espacio,
                        topRight + safe.right,
                        topBottom
                );
                inferior.setPadding(
                        bottomLeft + safe.left,
                        bottomTop,
                        bottomRight + safe.right,
                        bottomBottom + safe.bottom + espacio
                );
            }

            return windowInsets;
        });
        ViewCompat.requestApplyInsets(content);
    }
}
