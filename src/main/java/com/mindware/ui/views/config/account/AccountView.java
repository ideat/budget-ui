package com.mindware.ui.views.config.account;

import com.mindware.backend.entity.config.Account;
import com.mindware.backend.rest.account.AccountRestTemplate;
import com.mindware.backend.util.UtilValues;
import com.mindware.ui.MainLayout;
import com.mindware.ui.components.FlexBoxLayout;
import com.mindware.ui.components.detailsdrawer.DetailsDrawer;
import com.mindware.ui.components.detailsdrawer.DetailsDrawerFooter;
import com.mindware.ui.components.detailsdrawer.DetailsDrawerHeader;
import com.mindware.ui.layout.size.Horizontal;
import com.mindware.ui.layout.size.Top;
import com.mindware.ui.util.LumoStyles;
import com.mindware.ui.util.UIUtils;
import com.mindware.ui.util.css.BoxSizing;
import com.mindware.ui.views.SplitViewFrame;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@Route(value = "account", layout = MainLayout.class)
@ParentLayout(MainLayout.class)
@PageTitle("Cuentas")
public class AccountView extends SplitViewFrame implements RouterLayout {

    @Autowired
    private AccountRestTemplate restTemplate;

    @Autowired
    private UtilValues utilValues;

    private Button btnNew;

    List<Account> accountList;

    private Grid<Account> grid;
    private TextField numberAccountFilter;
    private TextField nameAccountFilter;
    private ComboBox<String> currencyFilter;
    private ComboBox<Integer> periodFilter;
    private ComboBox<String> cmbPeriod;

    private ListDataProvider<Account> dataProvider;
    private Binder<Account> binder;

    private DetailsDrawer detailsDrawer;
    private DetailsDrawerHeader detailsDrawerHeader;
    private DetailsDrawerFooter footer;

    private DetailsDrawer detailsDrawerCloneAccount;
    private DetailsDrawerHeader detailDrawHeaderCloneAccount;

    private Account current;

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        getAccounts();
        setViewHeader(createTopBar());
        setViewContent(createContent());
        setViewDetails(createDetailsDrawer());
    }

    private HorizontalLayout createTopBar(){
        btnNew = new Button("Nueva Cuenta");
        btnNew.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnNew.setIcon(VaadinIcon.PLUS_CIRCLE.create());
        btnNew.addClickShortcut(Key.KEY_N, KeyModifier.ALT);
        btnNew.addClickListener(e -> {
            if(!cmbPeriod.isEmpty()) {
                showDetails(new Account());
            }else{
                UIUtils.showNotificationType("Seleccione Periodo","alert");
            }
        });

        cmbPeriod = new ComboBox<>();
        cmbPeriod.setPlaceholder("Seleccione Periodo");
        cmbPeriod.setWidth("400px");
        cmbPeriod.setItems(utilValues.getValueParameterByCategory("PERIODO"));
        cmbPeriod.setAutoOpen(true);

        Button btnClone = new Button("Copiar Cuentas");
        btnClone.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_CONTRAST);
        btnClone.addClickListener(event -> {
           showCloneAccount();
        });

        HorizontalLayout topLayout = new HorizontalLayout();
        topLayout.setWidth("100%");
        topLayout.add(cmbPeriod, btnNew, btnClone);
        topLayout.setVerticalComponentAlignment(FlexComponent.Alignment.END,btnNew, btnClone);
        topLayout.setSpacing(true);
        topLayout.setPadding(true);

        return topLayout;
    }

    private Component createContent(){
        FlexBoxLayout content = new FlexBoxLayout(createGrid());
        content.addClassName("grid-view");
        content.setBoxSizing(BoxSizing.BORDER_BOX);
        content.setHeightFull();
        content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        return content;
    }

    private void showDetails(Account account){
        current = account;
        detailsDrawerHeader.setTitle("Cuenta: ".concat(account.getNumberAccount()==null?"Nueva":account.getNumberAccount()));
        detailsDrawer.setContent(createDetails(account));
        detailsDrawer.show();
        binder.readBean(account);
    }

    private DetailsDrawer createDetailsDrawer(){
        detailsDrawer = new DetailsDrawer(DetailsDrawer.Position.RIGHT);

        // Header
        detailsDrawerHeader = new DetailsDrawerHeader("");
        detailsDrawerHeader.addCloseListener(buttonClickEvent -> detailsDrawer.hide());
        detailsDrawer.setHeader(detailsDrawerHeader);

        footer = new DetailsDrawerFooter();
        footer.addSaveListener(e ->{
            if (current !=null && binder.writeBeanIfValid(current)){
                if(current.getPeriod()==null) {
                    current.setPeriod(Integer.parseInt(cmbPeriod.getValue()));
                }
                Account result = restTemplate.add(current);
                if (current.getId()==null){

                    accountList.add(result);
                    grid.getDataProvider().refreshAll();
                }else{
                    grid.getDataProvider().refreshItem(current);
                }
                detailsDrawer.hide();
            }else{
                UIUtils.showNotificationType("Datos incorrectos, verifique nuevamente","alert");
            }
        });

        footer.addCancelListener(e ->{
            footer.saveState(false);
            detailsDrawer.hide();
        });

        detailsDrawer.setFooter(footer);
        return detailsDrawer;
    }


    private Grid createGrid(){
        grid = new Grid();
        grid.setSizeFull();
        grid.setDataProvider(dataProvider);
        grid.addSelectionListener(event -> {
            event.getFirstSelectedItem().ifPresent(this::showDetails);
        });

        grid.addColumn(Account::getNumberAccount)
                .setFlexGrow(1)
                .setKey("numberAccount")
                .setHeader("Nro. Cuenta")
                .setSortable(true)
                .setAutoWidth(true)
                .setResizable(true);
        grid.addColumn(Account::getNameAccount)
                .setFlexGrow(1)
                .setKey("nameAccount")
                .setHeader("Nombre Cuenta")
                .setSortable(true)
                .setAutoWidth(true)
                .setResizable(true);
        grid.addColumn(Account::getCurrency)
                .setFlexGrow(1)
                .setKey("currency")
                .setHeader("Moneda")
                .setSortable(true)
                .setAutoWidth(true)
                .setResizable(true);
        grid.addColumn(Account::getPeriod)
                .setFlexGrow(1)
                .setKey("period")
                .setHeader("Periodo")
                .setSortable(true)
                .setAutoWidth(true)
                .setResizable(true);
        grid.addColumn(new ComponentRenderer<>(this::createButtonSubAccount))
                .setFlexGrow(1)
                .setTextAlign(ColumnTextAlign.START);


        HeaderRow hr = grid.appendHeaderRow();

        numberAccountFilter = new TextField();
        numberAccountFilter.setValueChangeMode(ValueChangeMode.EAGER);
        numberAccountFilter.setWidthFull();
        numberAccountFilter.addValueChangeListener(e -> {
            applyFilter(dataProvider);
        });
        hr.getCell(grid.getColumnByKey("numberAccount")).setComponent(numberAccountFilter);

        nameAccountFilter = new TextField();
        nameAccountFilter.setValueChangeMode(ValueChangeMode.EAGER);
        nameAccountFilter.setWidthFull();
        nameAccountFilter.addValueChangeListener(e -> applyFilter(dataProvider));
        hr.getCell(grid.getColumnByKey("nameAccount")).setComponent(nameAccountFilter);

        currencyFilter = new ComboBox<>();
        currencyFilter.setItems(utilValues.getValueParameterByCategory("MONEDA"));
        currencyFilter.setWidthFull();
        currencyFilter.addValueChangeListener(e -> {
           applyFilter(dataProvider);
        });
        hr.getCell(grid.getColumnByKey("currency")).setComponent(currencyFilter);

        periodFilter = new ComboBox<>();
        periodFilter.setItems( utilValues.getValueIntParameterByCategory("PERIODO"));
        periodFilter.setWidthFull();
        periodFilter.addValueChangeListener(e -> applyFilter(dataProvider));
        hr.getCell(grid.getColumnByKey("period")).setComponent(periodFilter);

        return grid;
    }

    private Component createButtonSubAccount(Account account){
        Button btn = new Button("Sub-Cuenta");
        btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_SUCCESS);
        btn.addClickListener(e -> {
            Map<String, List<String>> param = new HashMap<>();
            List<String> id = new ArrayList<>();
            id.add(account.getId().toString());
            List<String> nameAccount = new ArrayList<>();
            nameAccount.add(account.getNameAccount());

            param.put("id",id);
            param.put("nameAccount",nameAccount);

            QueryParameters qp = new QueryParameters(param);
            UI.getCurrent().navigate("subAccount",qp);
        });

        return btn;
    }

    private FormLayout createDetails(Account account){

        TextField numberAccount = new TextField();
        numberAccount.setWidthFull();
        numberAccount.setRequired(true);

        TextField nameAccount = new TextField();
        nameAccount.setWidthFull();
        nameAccount.setRequired(true);

        ComboBox<String> currency = new ComboBox<>();
        currency.setWidthFull();
        currency.setRequired(true);
        currency.setItems(utilValues.getValueParameterByCategory("MONEDA"));

        binder = new BeanValidationBinder<>(Account.class);
        binder.forField(numberAccount)
                .asRequired("Número de cuenta es requerido")
                .bind(Account::getNumberAccount,Account::setNumberAccount);
        binder.forField(nameAccount)
                .asRequired("Nombre cuenta es requerido")
                .bind(Account::getNameAccount,Account::setNameAccount);
        binder.forField(currency)
                .asRequired("Moneda es requerida")
                .bind(Account::getCurrency,Account::setCurrency);

        binder.addStatusChangeListener(event -> {
           boolean isValid = !event.hasValidationErrors();
           boolean hasChanges = binder.hasChanges();
           footer.saveState(hasChanges && isValid);
        });

        FormLayout form = new FormLayout();
        form.addClassNames(LumoStyles.Padding.Bottom.L,
                LumoStyles.Padding.Horizontal.S, LumoStyles.Padding.Top.S);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1,
                        FormLayout.ResponsiveStep.LabelsPosition.TOP),
                new FormLayout.ResponsiveStep("21em", 2,
                        FormLayout.ResponsiveStep.LabelsPosition.TOP));

        FormLayout.FormItem numberAccountItem = form.addFormItem(numberAccount,"Nro. cuenta");
        UIUtils.setColSpan(2,numberAccountItem);
        FormLayout.FormItem nameAccountItem = form.addFormItem(nameAccount,"Nombre de cuenta");
        UIUtils.setColSpan(2,nameAccountItem);
        FormLayout.FormItem currencyItem = form.addFormItem(currency,"Moneda");
        UIUtils.setColSpan(2,currencyItem);

        return form;
    }

    private DetailsDrawer createDetailDrawCloneAccount(){
        detailsDrawerCloneAccount = new DetailsDrawer(DetailsDrawer.Position.RIGHT);

        detailDrawHeaderCloneAccount = new DetailsDrawerHeader("");
        detailDrawHeaderCloneAccount.addCloseListener(event -> detailsDrawerCloneAccount.hide());
        detailsDrawerCloneAccount.setHeader(detailDrawHeaderCloneAccount);


        return detailsDrawerCloneAccount;
    }

    private VerticalLayout createFormCloneAccount(){
        VerticalLayout layout = new VerticalLayout();

        ComboBox<String> cmbPosting = new ComboBox<>("Periodo Destino");
        cmbPosting.setAutoOpen(true);
        cmbPosting.setWidthFull();

        ComboBox<String> cmbOriginal = new ComboBox<>("Periodo Origen");
        cmbOriginal.setAutoOpen(true);
        cmbOriginal.setWidthFull();
        cmbOriginal.setItems(utilValues.getValueParameterByCategory("PERIODO"));
        cmbOriginal.addValueChangeListener(event ->{
           List<String> listPosting =  utilValues.getValueParameterByCategory("PERIODO");
           listPosting.remove(event.getValue());
           cmbPosting.clear();
           cmbPosting.setItems(listPosting);
        });

        Button btnCopy = new Button("Copiar");
        btnCopy.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnCopy.addClickListener(event -> {
            try {
                List<Account> list = restTemplate.cloneAccount(Integer.parseInt(cmbOriginal.getValue()), Integer.parseInt(cmbPosting.getValue()));
                accountList.addAll(list);
                dataProvider.refreshAll();
                detailsDrawerCloneAccount.hide();
                Notification notification = new Notification();
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                notification.setPosition(Notification.Position.TOP_CENTER);
                notification.show("Cuentas Copiadas");
            }catch(Exception e){
                Notification notification = Notification.show(e.getMessage());
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                notification.setPosition(Notification.Position.TOP_CENTER);
            }
        });
        layout.add(cmbOriginal,cmbPosting,btnCopy);


        return layout;
    }

    private void showCloneAccount(){
        setViewDetails(createDetailDrawCloneAccount());
        setViewDetailsPosition(Position.RIGHT);
        detailDrawHeaderCloneAccount.setTitle("Copiar Cuentas");
        detailsDrawerCloneAccount.setContent(createFormCloneAccount());
        detailsDrawerCloneAccount.show();
    }


    private void applyFilter(ListDataProvider<Account> dataProvider){
        dataProvider.clearFilters();
        if(!numberAccountFilter.getValue().trim().equals("")){
            dataProvider.addFilter(account -> StringUtils.containsIgnoreCase(account.getNumberAccount(),numberAccountFilter.getValue().trim()));
        }
        if(!nameAccountFilter.getValue().trim().equals("")){
            dataProvider.addFilter(account -> StringUtils.containsIgnoreCase(account.getNameAccount(),nameAccountFilter.getValue().trim()));
        }
        if (currencyFilter.getValue()!=null){
            dataProvider.addFilter(account -> Objects.equals(currencyFilter.getValue(),account.getCurrency()));
        }
        if (periodFilter.getValue()!=null){
            dataProvider.addFilter(account -> Objects.equals(periodFilter.getValue(),account.getPeriod()));
        }
    }

    private void getAccounts(){
        accountList = new ArrayList<>(restTemplate.getAll());
        dataProvider = new ListDataProvider<>(accountList);
    }
}