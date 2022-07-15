package com.mindware.ui.views.login;

import com.mindware.backend.entity.rol.Rol;
import com.mindware.backend.rest.authentication.AuthenticateRestTemplate;
import com.mindware.backend.rest.authentication.JwtRequest;
import com.mindware.backend.rest.authentication.Token;
import com.mindware.backend.rest.authentication.UserData;
import com.mindware.backend.rest.rol.RolRestTemplate;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@Route("")
@RouteAlias("login")
public class LoginView extends VerticalLayout {

    @Autowired
    AuthenticateRestTemplate restTemplate;

    @Autowired
    private RolRestTemplate rolRestTemplate;

    private final LoginI18n i18n = LoginI18n.createDefault();

    @Override
    protected void onAttach(AttachEvent attachEvent){
        LoginForm component = new LoginForm();
        component.setI18n(createSpanishI18n());

        component.addLoginListener(e -> {
            JwtRequest jwtRequest = new JwtRequest();
            jwtRequest.setUsername(e.getUsername());
            jwtRequest.setPassword(e.getPassword());
            try {
                Token token = restTemplate.getToken(jwtRequest);
                VaadinSession.getCurrent().setAttribute("jwt", token.getToken());
                VaadinSession.getCurrent().setAttribute("login", e.getUsername());

                UserData userData = restTemplate.getDataUser(jwtRequest);

                VaadinSession.getCurrent().setAttribute("cn", userData.getCn());
                VaadinSession.getCurrent().setAttribute("sn", userData.getSn());
                VaadinSession.getCurrent().setAttribute("department", userData.getDepartment());
                VaadinSession.getCurrent().setAttribute("memberOf",userData.getMemberOf());
                VaadinSession.getCurrent().setAttribute("version","Versión 1.0.9");

                List<Rol> rolList = rolRestTemplate.getAllRols();
                for(Rol rol:rolList){
                    if( VaadinSession.getCurrent().getAttribute("memberOf").toString().contains(rol.getName())){
                        VaadinSession.getCurrent().setAttribute("options",rol.getOptions());
                        VaadinSession.getCurrent().setAttribute("scope",rol.getScope());
                        break;
                    }
                }

                UI.getCurrent().navigate("main");
            }catch (Exception ex){
                component.setError(true);
            }
        });

        setSizeFull();
        getStyle().set("background","url(images/background-login.jpg");
        getStyle().set("align-center","stretch");
        setHorizontalComponentAlignment(Alignment.CENTER,component);
        add(component);
    }

    private LoginI18n createSpanishI18n() {

        i18n.setHeader(new LoginI18n.Header());
        i18n.getHeader().setDescription("Sistema Presupuestario");
        i18n.getForm().setUsername("Usuario");
        i18n.getForm().setTitle("BUDGET");
        i18n.getForm().setSubmit("Entrar");
        i18n.getForm().setPassword("Clave");
        i18n.getForm().setForgotPassword("");
        i18n.getErrorMessage().setTitle("Usuario/clave inválida");
        i18n.getErrorMessage()
                .setMessage("Comprueba tu usuario y contraseña y vuelva a intentarlo.");
        i18n.setAdditionalInformation("");
        return i18n;
    }
}
