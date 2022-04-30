package com.mindware.ui.views.acquisition;

import com.mindware.ui.MainLayout;
import com.mindware.ui.views.SplitViewFrame;
import com.vaadin.flow.router.*;

@Route(value = "acquisition-register", layout = MainLayout.class)
@ParentLayout(MainLayout.class)
@PageTitle("Registro Adquisiciones")
public class AcquisitionRegisterView extends SplitViewFrame implements RouterLayout, HasUrlParameter<String> {



    @Override
    public void setParameter(BeforeEvent beforeEvent, String s) {

    }
}
