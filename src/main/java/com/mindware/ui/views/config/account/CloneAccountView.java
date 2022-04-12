package com.mindware.ui.views.config.account;

import com.mindware.backend.rest.account.AccountRestTemplate;
import com.mindware.ui.MainLayout;
import com.mindware.ui.views.SplitViewFrame;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "clone-account", layout = MainLayout.class)
@ParentLayout(MainLayout.class)
@PageTitle("Copiar cuentas")
public class CloneAccountView extends SplitViewFrame implements RouterLayout {

    @Autowired
    private AccountRestTemplate restTemplate;


}
