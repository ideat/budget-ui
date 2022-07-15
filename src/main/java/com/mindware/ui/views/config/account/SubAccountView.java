package com.mindware.ui.views.config.account;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindware.backend.entity.config.Account;
import com.mindware.backend.entity.config.SubAccount;
import com.mindware.backend.rest.account.AccountRestTemplate;
import com.mindware.backend.util.GrantOptions;
import com.mindware.ui.MainLayout;
import com.mindware.ui.components.DialogSweetAlert;
import com.mindware.ui.components.FlexBoxLayout;
import com.mindware.ui.components.detailsdrawer.DetailsDrawer;
import com.mindware.ui.components.detailsdrawer.DetailsDrawerFooter;
import com.mindware.ui.components.detailsdrawer.DetailsDrawerHeader;
import com.mindware.ui.components.navigation.bar.AppBar;
import com.mindware.ui.layout.size.Horizontal;
import com.mindware.ui.layout.size.Top;
import com.mindware.ui.util.LumoStyles;
import com.mindware.ui.util.UIUtils;
import com.mindware.ui.util.css.BoxSizing;
import com.mindware.ui.views.SplitViewFrame;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.*;
import com.wontlost.sweetalert2.Config;
import com.wontlost.sweetalert2.SweetAlert2Vaadin;
import dev.mett.vaadin.tooltip.Tooltips;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Route(value = "subAccount", layout = MainLayout.class)
@PageTitle("Subcuenta")
public class SubAccountView extends SplitViewFrame implements HasUrlParameter<String>, RouterLayout {

    @Autowired
    private AccountRestTemplate restTemplate;

    private DetailsDrawer detailsDrawer;
    private DetailsDrawerHeader detailsDrawerHeader;
    private DetailsDrawerFooter footer;

    private Binder<SubAccount> binder;
    private ListDataProvider<SubAccount> dataProvider;
    private Grid<SubAccount> grid;

    private ObjectMapper mapper;
    private Map<String, List<String>> param;

    private Account account;
    private List<SubAccount> subAccountList;

    private SubAccount current,initial;

    @SneakyThrows
    @Override
    public void setParameter(BeforeEvent beforeEvent, @OptionalParameter String s) {
        mapper = new ObjectMapper();
        Location location = beforeEvent.getLocation();
        QueryParameters queryParameters = location.getQueryParameters();

        param = queryParameters.getParameters();

        account = restTemplate.getById(param.get("id").get(0));
        String jsonSubAccount =  account.getSubAccount();

        subAccountList = mapper.readValue(jsonSubAccount, new TypeReference<List<SubAccount>>() {});

    }

    @Override
    protected void onAttach(AttachEvent attachEvent){
        super.onAttach(attachEvent);
        dataProvider = new ListDataProvider<>(subAccountList);
        AppBar appBar = initBar();
        appBar.setTitle(param.get("nameAccount").get(0));

        setViewHeader(createTopBar());
        setViewContent(createContent());
        setViewDetails(createDetailsDrawer());

    }

    private AppBar initBar(){
        AppBar appBar = MainLayout.get().getAppBar();
        appBar.setNaviMode(AppBar.NaviMode.CONTEXTUAL);
        appBar.getContextIcon().addClickListener(e -> UI.getCurrent().navigate(AccountView.class));

        return appBar;
    }

    private HorizontalLayout createTopBar() {
        Button btnNew = new Button("Nuevo");
        btnNew.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnNew.setIcon(VaadinIcon.PLUS_CIRCLE.create());
        btnNew.addClickShortcut(Key.KEY_N, KeyModifier.ALT);
        btnNew.setVisible(GrantOptions.grantedOptionWrite("Cuentas"));
        btnNew.addClickListener(e->showDetails(new SubAccount()));

        HorizontalLayout topLayout = new HorizontalLayout();
        topLayout.setWidth("100%");
        topLayout.add(btnNew);
        topLayout.setVerticalComponentAlignment(FlexComponent.Alignment.END,btnNew);
        topLayout.setSpacing(true);
        topLayout.setPadding(true);

        return topLayout;
    }

    private void showDetails(SubAccount subAccount){
        current = subAccount;
        initial = subAccount;
        detailsDrawerHeader.setTitle(subAccount.getId()==null?"Subcuenta: Nueva":"Subcuenta: "+ subAccount.getNumberSubAccount());
        detailsDrawer.setContent(createDetails(current));
        detailsDrawer.show();
        binder.readBean(current);
    }

    private DetailsDrawer createDetailsDrawer(){
        detailsDrawer = new DetailsDrawer(DetailsDrawer.Position.RIGHT);
        detailsDrawerHeader = new DetailsDrawerHeader("");
        detailsDrawer.setHeader(detailsDrawerHeader);
        detailsDrawerHeader.addCloseListener(event -> detailsDrawer.hide());

        footer = new DetailsDrawerFooter();
        footer.addSaveListener(e ->{
            if(binder.writeBeanIfValid(current)){
                Optional<SubAccount> searchNameSubAccount = subAccountList.stream()
                        .filter(s -> s.getNameSubAccount().equals(current.getNameSubAccount()))
                        .findFirst();
                Optional<SubAccount> searchNumberSubAccount = subAccountList.stream()
                        .filter(s -> s.getNumberSubAccount().equals(current.getNumberSubAccount()))
                        .findFirst();
                if(searchNumberSubAccount.isPresent() && !searchNumberSubAccount.get().getId().equals(current.getId())){
                    UIUtils.showNotificationType("Número Subcuenta ya se ecuentra registrado ","alert");
                    return;
                }
                if(searchNameSubAccount.isPresent() && !searchNameSubAccount.get().getId().equals(current.getId())){
                    UIUtils.showNotificationType("Nombre Subcuenta ya se ecuentra registrado ","alert");
                    return;
                }

                if(current.getId()==null){
                    current.setId(UUID.randomUUID());
                }else{
                    subAccountList.remove(initial);
                }

                subAccountList.add(current);
                try {
                    String jsonSubAccount = mapper.writeValueAsString(subAccountList);
                    account.setSubAccount(jsonSubAccount);
                } catch (JsonProcessingException jsonProcessingException) {
                    jsonProcessingException.printStackTrace();
                }
                detailsDrawer.hide();
                dataProvider.refreshAll();

                restTemplate.add(account);

            }
        });

        footer.addCancelListener(e ->{
            footer.saveState(false);
            detailsDrawer.hide();
        });
        detailsDrawer.setFooter(footer);
        return detailsDrawer;
    }

    private FormLayout createDetails(SubAccount subAccount){
        TextField numberSubAccount = new TextField();
        numberSubAccount.setWidthFull();
        numberSubAccount.setRequired(true);

        TextField nameSubAccount = new TextField();
        nameSubAccount.setWidthFull();
        nameSubAccount.setRequired(true);

        binder = new BeanValidationBinder<>(SubAccount.class);
        binder.forField(numberSubAccount)
                .asRequired("Número Subcuenta es requerido")
                .withValidator(l -> l.trim().length()>0,"Nro. Subcuenta debe tener al menos 1 caracter")
                .bind(SubAccount::getNumberSubAccount,SubAccount::setNumberSubAccount);
        binder.forField(nameSubAccount)
                .asRequired("Nombre Subcuenta es requerido")
                .withValidator(l -> l.trim().length()>0,"Nombre. Subuenta debe tener al menos 1 caracter")
                .bind(SubAccount::getNameSubAccount,SubAccount::setNameSubAccount);
        binder.addStatusChangeListener(event -> {
            boolean isValid = !event.hasValidationErrors();
            boolean hasChanges= binder.hasChanges();
            footer.saveState(isValid && hasChanges && GrantOptions.grantedOptionWrite("Cuentas"));
        });

        FormLayout form = new FormLayout();
        form.addClassNames(LumoStyles.Padding.Bottom.L,
                LumoStyles.Padding.Horizontal.S, LumoStyles.Padding.Top.S);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1,
                        FormLayout.ResponsiveStep.LabelsPosition.TOP),
                new FormLayout.ResponsiveStep("21em", 2,
                        FormLayout.ResponsiveStep.LabelsPosition.TOP));

        FormLayout.FormItem numberSubAccountItem = form.addFormItem(numberSubAccount,"Nro. Subcuenta");
        UIUtils.setColSpan(2,numberSubAccountItem);
        FormLayout.FormItem nameSubAccountItem = form.addFormItem(nameSubAccount,"Nombre Subcuenta");
        UIUtils.setColSpan(2,nameSubAccountItem);

        return form;
    }

    private Component createContent(){
        FlexBoxLayout content = new FlexBoxLayout(createGridSubAccount());
        content.addClassName("grid-view");
        content.setBoxSizing(BoxSizing.BORDER_BOX);
        content.setHeightFull();
        content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        return content;
    }

    private Grid createGridSubAccount(){
        grid = new Grid<>();
//        grid.addSelectionListener(event -> event.getFirstSelectedItem().ifPresent(this::showDetails));
        grid.setDataProvider(dataProvider);
        grid.setSizeFull();

        grid.addColumn(SubAccount::getNumberSubAccount)
                .setFlexGrow(1)
                .setHeader("Número Subcuenta")
                .setSortable(true)
                .setResizable(true)
                .setAutoWidth(true);
        grid.addColumn(SubAccount::getNameSubAccount)
                .setFlexGrow(1)
                .setHeader("Nombre Subcuenta")
                .setSortable(true)
                .setResizable(true)
                .setAutoWidth(true);
        grid.addColumn(new ComponentRenderer<>(this::createButtonEdit))
                .setFlexGrow(1)
                .setAutoWidth(true);
        grid.addColumn(new ComponentRenderer<>(this::createButtonDelete))
                .setFlexGrow(1)
                .setAutoWidth(true);
        return grid;
    }

    private Component createButtonEdit(SubAccount subAccount){
        Button btn = new Button();
        Tooltips.getCurrent().setTooltip(btn,"Editar Registro");
        btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_SUCCESS);
        btn.setIcon(VaadinIcon.EDIT.create());
        btn.setVisible(GrantOptions.grantedOptionWrite("Cuentas"));
        btn.addClickListener(event -> {
            showDetails(subAccount);
        });
        return btn;
    }

    private Component createButtonDelete(SubAccount subAccount){
        Button btn = new Button();
        btn.setIcon(VaadinIcon.TRASH.create());
        btn.addThemeVariants(ButtonVariant.LUMO_ERROR,ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_SMALL);
        btn.setVisible(GrantOptions.grantedOptionWrite("Cuentas"));
        Tooltips.getCurrent().setTooltip(btn,"Eliminar");
        btn.addClickListener(event -> {

            SweetAlert2Vaadin sweetAlert2Vaadin = new DialogSweetAlert().dialogConfirm("Eliminar Registro","Deseas Eliminar la Subcuenta "+ subAccount.getNameSubAccount() + "?\"");

            sweetAlert2Vaadin.open();
            sweetAlert2Vaadin.addConfirmListener(e -> {
                subAccountList.remove(subAccount);
                grid.getDataProvider().refreshAll();
                footer.saveState(GrantOptions.grantedOptionWrite("Cuentas"));
            });
            sweetAlert2Vaadin.addCancelListener(e -> e.getSource().close());

        });

        return btn;
    }

}
