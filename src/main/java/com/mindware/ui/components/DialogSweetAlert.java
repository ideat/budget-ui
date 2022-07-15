package com.mindware.ui.components;

import com.wontlost.sweetalert2.Config;
import com.wontlost.sweetalert2.SweetAlert2Vaadin;

public class DialogSweetAlert {


    public SweetAlert2Vaadin dialogConfirm(String title, String text){
        Config config = new Config();
        config.setTitle(title);
        config.setText(text);
        config.setAllowEscapeKey(true);
        config.setCancelButtonText("Cancelar");
        config.setConfirmButtonText("Aceptar");
        config.setShowCancelButton(true);
        config.setShowConfirmButton(true);
        SweetAlert2Vaadin sweetAlert2Vaadin = new SweetAlert2Vaadin(config);
        return sweetAlert2Vaadin;
    }
}
