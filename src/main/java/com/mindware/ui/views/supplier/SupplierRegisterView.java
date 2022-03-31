package com.mindware.ui.views.supplier;

import com.mindware.backend.rest.supplier.SupplierRestTemplate;
import com.mindware.ui.MainLayout;
import com.mindware.ui.views.SplitViewFrame;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.router.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "supplier-register", layout = MainLayout.class)
@PageTitle("Registro Proveedor")
public class SupplierRegisterView extends SplitViewFrame implements HasUrlParameter<String>, RouterLayout {

    @Autowired
    private SupplierRestTemplate restTemplate;

    @Override
    public void setParameter(BeforeEvent beforeEvent, @OptionalParameter String s) {

    }

    @Override
    protected void onAttach(AttachEvent attachEvent){
        super.onAttach(attachEvent);

    }
}
