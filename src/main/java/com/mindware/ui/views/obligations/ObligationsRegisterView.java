package com.mindware.ui.views.obligations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindware.backend.entity.commonJson.ExpenseDistribuite;
import com.mindware.backend.entity.config.BasicServiceProvider;
import com.mindware.backend.entity.config.Parameter;
import com.mindware.backend.entity.corebank.Concept;
import com.mindware.backend.entity.historyEndedOperations.HistoryEndedOperations;
import com.mindware.backend.entity.invoiceAuthorizer.InvoiceAuthorizer;
import com.mindware.backend.entity.invoiceAuthorizer.SelectedInvoiceAuthorizer;
import com.mindware.backend.entity.obligations.Obligations;
import com.mindware.backend.entity.obligations.ObligationsDto;
import com.mindware.backend.rest.basicServiceProvider.BasicServiceProviderRestTemplate;
import com.mindware.backend.rest.corebank.ConceptRestTemplate;
import com.mindware.backend.rest.historyEndedOperations.HistoryEndedOperationsRestTemplate;
import com.mindware.backend.rest.invoiceAuthorizer.InvoiceAuthorizerRestTemplate;
import com.mindware.backend.rest.obligations.ObligationsDtoRestTemplate;
import com.mindware.backend.rest.obligations.ObligationsRestTemplate;
import com.mindware.backend.rest.parameter.ParameterRestTemplate;
import com.mindware.backend.rest.supplier.SupplierRestTemplate;
import com.mindware.backend.util.GrantOptions;
import com.mindware.backend.util.UtilValues;
import com.mindware.ui.MainLayout;
import com.mindware.ui.components.DialogSweetAlert;
import com.mindware.ui.components.FlexBoxLayout;
import com.mindware.ui.components.detailsdrawer.DetailsDrawer;
import com.mindware.ui.components.detailsdrawer.DetailsDrawerFooter;
import com.mindware.ui.components.detailsdrawer.DetailsDrawerHeader;
import com.mindware.ui.components.navigation.bar.AppBar;
import com.mindware.ui.layout.size.Left;
import com.mindware.ui.layout.size.Right;
import com.mindware.ui.layout.size.Top;
import com.mindware.ui.layout.size.Vertical;
import com.mindware.ui.util.LumoStyles;
import com.mindware.ui.util.UIUtils;
import com.mindware.ui.views.SplitViewFrame;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinSession;
import com.wontlost.sweetalert2.SweetAlert2Vaadin;
import dev.mett.vaadin.tooltip.Tooltips;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Route(value = "obligations-register", layout = MainLayout.class)
@ParentLayout(MainLayout.class)
@PageTitle("Registro Pago Obligaciones")
public class ObligationsRegisterView extends SplitViewFrame implements HasUrlParameter<String>, RouterLayout {

    @Autowired
    private ObligationsRestTemplate obligationsRestTemplate;

    @Autowired
    private ObligationsDtoRestTemplate obligationsDtoRestTemplate;

//    @Autowired
//    private SupplierRestTemplate supplierRestTemplate;

    @Autowired
    private BasicServiceProviderRestTemplate basicServiceProviderRestTemplate;

    @Autowired
    private UtilValues utilValues;

    @Autowired
    private ConceptRestTemplate conceptRestTemplate;

    @Autowired
    private InvoiceAuthorizerRestTemplate invoiceAuthorizerTemplate;

    @Autowired
    private ParameterRestTemplate parameterRestTemplate;

    @Autowired
    private HistoryEndedOperationsRestTemplate historyEndedOperationsRestTemplate;

    private ObligationsDto obligationsDto;
    private BasicServiceProvider basicServiceProviderSelected;
    private List<ExpenseDistribuite> expenseDistribuiteList;

    private ListDataProvider<ExpenseDistribuite> expenseDistribuiteDataProvider;
    private Binder<ObligationsDto> binder;
    private Binder<ExpenseDistribuite> expenseDistribuiteBinder;
    private ExpenseDistribuite currentExpenseDistribuite;

    private ObjectMapper mapper;
    private Map<String, List<String>> param;
    private String title;

    private DetailsDrawerFooter footer;
    private DetailsDrawerFooter footerExpenseDistribuite;
    private DetailsDrawer detailsDrawer;
    private DetailsDrawerHeader detailsDrawerHeader;
    private DetailsDrawer detailsDrawerExpenseDistribuite;
    private DetailsDrawerHeader detailsDrawerHeaderExpenseDistribuite;

    private TextField nameSupplier;
    private TextField description;
    private ComboBox<String> account;
    private ComboBox<String> subAccount;
    private DatePicker paymentDate;

    private DatePicker dateDeliveryAccounting;
    private ComboBox<String> accountingPerson;

    private Grid<ExpenseDistribuite> expenseDistribuiteGrid;

    private TextField nameBasicProviderFilter;
    private TextField nitBasicProviderFilter;
    private TextField descriptionBasicProviderFilter;
    private TextField typeServiceBasicProviderFilter;

    private List<Concept> conceptList;

    private FlexBoxLayout contentCreateObligation;
    private FlexBoxLayout contentInvoiceAuthorizer;
    private FlexBoxLayout contentDeliveyAccounting;

    private String currentTab;

    private Grid<SelectedInvoiceAuthorizer> selectedInvoiceAuthorizerGrid;
    private SelectedInvoiceAuthorizer currentSelectedInvoiceAuthorizer;
    private DetailsDrawerHeader detailsDrawerHeaderSelectedInvoiceAuthorizer;
    private DetailsDrawer detailsDrawerSelectedInvoiceAuthorizer;
    private DetailsDrawerFooter footerInvoiceAuthorizer;
    private Binder<SelectedInvoiceAuthorizer> selectedInvoiceAuthorizerBinder;
    private List<SelectedInvoiceAuthorizer> selectedInvoiceAuthorizerList;
    private ListDataProvider<SelectedInvoiceAuthorizer> selectedInvoiceAuthorizerDataProvider;

    private List<String> typeObligationList = new ArrayList<>();
    private List<Parameter> parameterList = new ArrayList<>();

    @Override
    public void setParameter(BeforeEvent beforeEvent,  @OptionalParameter String s) {
//        typeObligationList.add("PATENTES");
//        typeObligationList.add("IMPUESTOS");
//        typeObligationList.add("OTRAS TASAS");
        typeObligationList.addAll(utilValues.getValueParameterByCategory("TIPO OBLIGACION"));
        parameterList.addAll(parameterRestTemplate.getByCategory("TIPO OBLIGACION"));

        mapper = new ObjectMapper();
        Location location = beforeEvent.getLocation();
        QueryParameters queryParameters = location.getQueryParameters();
        param = queryParameters.getParameters();
        footer = new DetailsDrawerFooter();

        if(!param.get("id").get(0).equals("NUEVO")){
            obligationsDto = obligationsDtoRestTemplate.getById(param.get("id").get(0));

            title = "Proveedor: ".concat(obligationsDto.getNameSupplier());
            basicServiceProviderSelected = basicServiceProviderRestTemplate.getById(obligationsDto.getIdSupplier().toString());

        }else{
            obligationsDto = new ObligationsDto();
            obligationsDto.setState("INICIADO");
            obligationsDto.setExpenseDistribuite("[]");
            obligationsDto.setInvoiceAuthorizer("[]");
            title = "Registro Nuevo";
        }

        try {
            expenseDistribuiteList = mapper.readValue(obligationsDto.getExpenseDistribuite(), new TypeReference<List<ExpenseDistribuite>>() {});
            selectedInvoiceAuthorizerList = mapper.readValue(obligationsDto.getInvoiceAuthorizer(), new TypeReference<List<SelectedInvoiceAuthorizer>>(){});
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        expenseDistribuiteDataProvider = new ListDataProvider<>(expenseDistribuiteList);
        selectedInvoiceAuthorizerDataProvider = new ListDataProvider<>(selectedInvoiceAuthorizerList);

        conceptList = new ArrayList<>(conceptRestTemplate.getAgencia());
//        conceptList.addAll(conceptRestTemplate.getSucursal());
        conceptList.sort(Comparator.comparing(Concept::getCode));

        contentCreateObligation = (FlexBoxLayout) createContent(createObligationDtoForm(obligationsDto));
        contentInvoiceAuthorizer = (FlexBoxLayout) createContent(createGridSelectedInvoiceAuthorizer());
        contentDeliveyAccounting = (FlexBoxLayout) createContent(createDeliverAccounting());

        setViewDetails(createDetailDrawer());
        setViewDetailsPosition(Position.BOTTOM);

        footer.addSaveListener(event -> {
            try {
                if(obligationsDto.getInvoiceAuthorizer() !=null && !obligationsDto.getInvoiceAuthorizer().equals("[]") && GrantOptions.grantedOptionAccounting("Obligaciones")){
                    binder.forField(dateDeliveryAccounting)
                            .asRequired("Fecha entrega a contabilidad es requerida")
                            .withValidator(d -> d.isAfter(paymentDate.getValue()),"Fecha Entrega Contabilidad no puede ser anterior a la Fecha de Pago")
                            .bind(ObligationsDto::getDateDeliveryAccounting, ObligationsDto::setDateDeliveryAccounting);
                    binder.forField(accountingPerson)
                            .asRequired("Responsable de contabilidad es requerido")
                            .bind(ObligationsDto::getAccountingPerson, ObligationsDto::setAccountingPerson);

                }

                if(binder.writeBeanIfValid(obligationsDto)){
                    obligationsDto.setIdSupplier(basicServiceProviderSelected.getId());


                    if(validateAmountExpenseDistribuite()) {
                        try{
                            Obligations obligations = null;
                            if(obligationsDto.getState().equals("FINALIZADO")){

                                try {
                                    HistoryEndedOperations historyEndedOperations = new HistoryEndedOperations();
                                    Obligations previousObligations = obligationsRestTemplate.getById(obligationsDto.getId().toString());
                                    historyEndedOperations.setIdOperation(obligationsDto.getId());
                                    historyEndedOperations.setTypeEntity(Obligations.class.getSimpleName());
                                    historyEndedOperations.setDateChanged(LocalDate.now());
                                    historyEndedOperations.setChangedBy(VaadinSession.getCurrent().getAttribute("login").toString());
                                    String previous = mapper.writeValueAsString(previousObligations);
                                    historyEndedOperations.setOriginalDataEntity(previous);

                                    obligations = obligationsRestTemplate.add(fillObligations());
                                    String current = mapper.writeValueAsString(obligations);
                                    historyEndedOperations.setChangedDataEntity(current);

                                    historyEndedOperationsRestTemplate.add(historyEndedOperations);



                                }catch (Exception ex){
                                    UIUtils.showNotificationType("Error al crear el historico. " +" \n  ","alert");

                                    return;
                                }
                            }else {

                                obligations = obligationsRestTemplate.add(fillObligations());
                                obligationsDto.setId(obligations.getId());
                                obligationsDto.setInvoiceAuthorizer(obligations.getInvoiceAuthorizer());
                            }

                        }catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
//                        UI.getCurrent().navigate(ObligationsView.class);
                        UIUtils.showNotificationType("Datos registrados", "success");
                    }else{
                        UIUtils.showNotificationType("Monto distribuido no coincide con factura , revisar","alert");
                    }
                }else {
                    UIUtils.showNotificationType("Datos no completos, revisar", "alert");
                }
            }catch (Exception e){
                String[] arrMsg = e.getMessage().split(",");
                String[] msg = arrMsg[1].split(":");
                UIUtils.showNotificationType(msg[1].replaceAll("\"",""),"alert");

            }
        });

        footer.addCancelListener(event ->  UI.getCurrent().navigate(ObligationsView.class));
        setViewFooter(footer);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent){
        super.onAttach(attachEvent);
        initBar();
        setViewContent(contentCreateObligation, contentInvoiceAuthorizer, contentDeliveyAccounting);
        binder.readBean(obligationsDto);

    }

    private AppBar initBar(){
        MainLayout.get().getAppBar().reset();
        AppBar appBar = MainLayout.get().getAppBar();

        appBar.addTab("Registro Factura Obligación");
        appBar.addTab("Autorización Factura");
        if(GrantOptions.grantedOptionAccounting("Obligaciones"))
            appBar.addTab("Entrega a Contabilidad");

        appBar.centerTabs();
        currentTab = "Registro Factura Obligación";
        hideContent();
        appBar.addTabSelectionListener(e -> {
            enabledSheets();
            if(e.getSource().getSelectedTab()!=null) {
                Tab selectTab = appBar.getSelectedTab();
                currentTab = selectTab.getLabel();
                hideContent();
            }
        });
        appBar.setNaviMode(AppBar.NaviMode.CONTEXTUAL);
        appBar.setTitle(title);
        appBar.getContextIcon().addClickListener(e -> UI.getCurrent().navigate("obligations"));

        return appBar;
    }

    private void enabledSheets(){
        if(obligationsDto.getId()==null){
            contentInvoiceAuthorizer.setEnabled(false);
        }else{
            contentInvoiceAuthorizer.setEnabled(true);
        }
        if(obligationsDto.getInvoiceAuthorizer()==null || obligationsDto.getInvoiceAuthorizer().equals("[]")){
            contentDeliveyAccounting.setEnabled(false);
        }else{
            contentDeliveyAccounting.setEnabled(true);
        }
    }

    private void hideContent(){
        contentCreateObligation.setVisible(true);
        contentInvoiceAuthorizer.setVisible(false);
        contentDeliveyAccounting.setVisible(false);
        if(currentTab.equals("Registro Factura Obligación")){
            contentCreateObligation.setVisible(true);
            contentInvoiceAuthorizer.setVisible(false);
            contentDeliveyAccounting.setVisible(false);
        }else if(currentTab.equals("Autorización Factura")){
            contentCreateObligation.setVisible(false);
            contentInvoiceAuthorizer.setVisible(true);
            contentDeliveyAccounting.setVisible(false);
        }else if(currentTab.equals("Entrega a Contabilidad")){
            contentCreateObligation.setVisible(false);
            contentInvoiceAuthorizer.setVisible(false);
            contentDeliveyAccounting.setVisible(true);
        }
    }

    private Component createContent(DetailsDrawer component){
        FlexBoxLayout content = new FlexBoxLayout(component);
        content.setFlexDirection(FlexLayout.FlexDirection.ROW);
        content.setMargin(Vertical.AUTO, Vertical.RESPONSIVE_L);
        content.setSizeFull();

        return content;
    }

    private DetailsDrawer createObligationDtoForm(ObligationsDto obligationsDto){
        ComboBox<String> period = new ComboBox<>();
        period.setWidthFull();
        period.setRequired(true);
        period.setAllowCustomValue(true);

        ComboBox<String> typeObligation = new ComboBox<>();
        typeObligation.setWidthFull();
        typeObligation.setItems(typeObligationList);
        typeObligation.addValueChangeListener(event -> {

            Optional<String> frecuency = parameterList.stream()
                    .filter(f -> f.getValue().equals(event.getValue()))
                    .map(Parameter::getDetails)
                    .findFirst();
            if(frecuency.isEmpty()){
                UIUtils.showNotificationType("Frecuencia no configurada en los parámetros","alert");
                return;
            }
            if(frecuency.get().equals("ANUAL") ){
                period.clear();
                period.setItems(utilValues.getAllYearsString());
            }else{
                period.clear();
                period.setItems(utilValues.generatePeriods());

            }
        });

        nameSupplier = new TextField();
        nameSupplier.setWidthFull();
        nameSupplier.setReadOnly(true);
        nameSupplier.setRequired(true);

        description = new TextField();
        description.setWidthFull();
        description.setRequired(true);



        paymentDate = new DatePicker();
        paymentDate.setWidthFull();
        paymentDate.setPlaceholder("DD/MM/AAAA");
        paymentDate.setRequired(true);
        paymentDate.setLocale(new Locale("es","BO"));
        paymentDate.setI18n(UIUtils.spanish());

        NumberField amount = new NumberField();
        amount.setWidthFull();
        amount.setRequiredIndicatorVisible(true);
        amount.setMin(0.0);

        account = new ComboBox<>();
        account.setWidthFull();
        account.setItems(utilValues.getNameAccounts());
        account.setRequired(true);
        account.setRequiredIndicatorVisible(true);
        account.addValueChangeListener(e -> {
            subAccount.clear();
            subAccount.setItems(utilValues.getNameSubAccounts(e.getValue()));
        });

        subAccount = new ComboBox<>();
        subAccount.setWidthFull();
        subAccount.setRequired(true);
        subAccount.setRequiredIndicatorVisible(true);


        ComboBox<String> typeDocumentReceived = new ComboBox<>();
        typeDocumentReceived.setWidthFull();
        typeDocumentReceived.setItems("FACTURA","CAABS");
        typeDocumentReceived.setRequired(true);

        TextField numberDocumentReceived = new TextField();
        numberDocumentReceived.setWidthFull();
        numberDocumentReceived.setRequiredIndicatorVisible(true);

        binder = new BeanValidationBinder<>(ObligationsDto.class);
        binder.forField(typeObligation)
                .asRequired("Tipo Obligación es requerido")
                .bind(ObligationsDto::getTypeObligation, ObligationsDto::setTypeObligation);
        binder.forField(description)
                .asRequired("Descripción es requerida")
                .withConverter(new UtilValues.StringTrimValue())
                .bind(ObligationsDto::getDescription,ObligationsDto::setDescription);
        binder.forField(nameSupplier)
                .asRequired("Proveedor es requerido")
                .withConverter(new UtilValues.StringTrimValue())
                .bind(ObligationsDto::getNameSupplier,ObligationsDto::setNameSupplier);
        binder.forField(period)
                .asRequired("Periodo es requerido")
                .bind(ObligationsDto::getPeriod,ObligationsDto::setPeriod);
        binder.forField(paymentDate)
                .asRequired("Fecha de Pago es requerida")
                .withValidator(d -> d.getYear()>= 2000,"Año incorrecto, ingrese 4 dígitos")
                .bind(ObligationsDto::getPaymentDate,ObligationsDto::setPaymentDate);
        binder.forField(amount)
                .asRequired("Monto es requerido")
                .withValidator(m -> m.doubleValue()>0.0,"Monto debe ser mayor a 0")
                .bind(ObligationsDto::getAmount,ObligationsDto::setAmount);
        binder.forField(account)
                .asRequired("Cuenta es requerida")
                .bind(ObligationsDto::getAccount,ObligationsDto::setAccount);
        binder.forField(subAccount)
                .asRequired("Subcuenta es requerida")
                .bind(ObligationsDto::getSubAccount,ObligationsDto::setSubAccount);
        binder.forField(typeDocumentReceived)
                .asRequired("Tipo Documento es requerido")
                .bind(ObligationsDto::getTypeDocumentReceived, ObligationsDto::setTypeDocumentReceived);
        binder.forField(numberDocumentReceived)
                .asRequired("Nro de Factura/CAABS es requerido")
                .withConverter(new UtilValues.StringTrimValue())
                .bind(ObligationsDto::getNumberDocumentReceived,ObligationsDto::setNumberDocumentReceived);

        binder.addStatusChangeListener(event -> {
            boolean isValid = !event.hasValidationErrors();
            boolean hasChanges = binder.hasChanges();
//            if(obligationsDto.getState()==null || !obligationsDto.getState().equals("FINALIZADO")) {
//
//                footer.saveState(isValid && hasChanges && GrantOptions.grantedOptionWrite("Obligaciones"));
//            }else{
//                footer.saveState(false);
//            }

            if(VaadinSession.getCurrent().getAttribute("memberOf").toString().contains("BUDGET_ADMIN")){
                footer.saveState(isValid && hasChanges && GrantOptions.grantedOptionWrite("Servicios Básicos"));
            }else {
                footer.saveState(isValid && hasChanges && GrantOptions.grantedOptionWrite("Servicios Básicos")
                && (obligationsDto.getState()==null || !obligationsDto.getState().equals("FINALIZADO")));
            }
        });

        FormLayout form = new FormLayout();
        form.setSizeUndefined();
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0",1,
                        FormLayout.ResponsiveStep.LabelsPosition.TOP),
                new FormLayout.ResponsiveStep("600px",2,
                        FormLayout.ResponsiveStep.LabelsPosition.TOP),
                new FormLayout.ResponsiveStep("810px",3,
                        FormLayout.ResponsiveStep.LabelsPosition.TOP),
                new FormLayout.ResponsiveStep("1024px",4,
                        FormLayout.ResponsiveStep.LabelsPosition.TOP)
        );
        form.addFormItem(typeObligation,"Tipo Obligación");
        HorizontalLayout layoutSupplier = new HorizontalLayout();
        Button btnSearchSupplier = new Button();
        btnSearchSupplier.setWidth("10%");
        btnSearchSupplier.addThemeVariants(ButtonVariant.LUMO_SMALL,ButtonVariant.LUMO_PRIMARY);
        btnSearchSupplier.setIcon(VaadinIcon.SEARCH_PLUS.create());
        btnSearchSupplier.addClickListener(event -> {
            setViewDetails(createDetailDrawer());
            setViewDetailsPosition(Position.BOTTOM);
            showSearchSupplier();
        });
        FormLayout.FormItem supplierItem = form.addFormItem(layoutSupplier,"Proveedor");
        UIUtils.setColSpan(2,supplierItem);
        layoutSupplier.add(nameSupplier,btnSearchSupplier);

        form.addFormItem(description,"Descripción");
        form.addFormItem(period,"Periodo");
        form.addFormItem(paymentDate,"Fecha de Pago");
        form.addFormItem(amount,"Monto (Bs)");
        form.addFormItem(account,"Cuenta");
        form.addFormItem(subAccount,"Subcuenta");
        form.addFormItem(typeDocumentReceived,"Tipo Documento");
        form.addFormItem(numberDocumentReceived,"Nro Factura/CAABS");

        DetailsDrawer detailsDrawer = new DetailsDrawer(DetailsDrawer.Position.BOTTOM);
        detailsDrawer.setHeight("100%");
        detailsDrawer.setPadding(Left.S, Right.S, Top.S);
        detailsDrawer.setContent(form, gridExpenseDistribuite());
//        detailsDrawer.setFooter(footer);
        detailsDrawer.show();


        return detailsDrawer;
    }

    private DetailsDrawer createDetailDrawer(){
        detailsDrawer = new DetailsDrawer(DetailsDrawer.Position.BOTTOM);
//        detailsDrawer.setWidthFull();
        // Header
        detailsDrawerHeader = new DetailsDrawerHeader("");
        detailsDrawerHeader.addCloseListener(buttonClickEvent -> detailsDrawer.hide());
        detailsDrawer.setHeader(detailsDrawerHeader);

        return detailsDrawer;
    }

    private void showSearchSupplier(){
        detailsDrawer.setPosition(DetailsDrawer.Position.BOTTOM);
        detailsDrawerHeader.setTitle("Seleccionar Proveedor");
        detailsDrawer.setContent(searchSupplier());
        detailsDrawer.show();
    }

    private Grid searchSupplier() {
        List<BasicServiceProvider> basicServiceProviderList = basicServiceProviderRestTemplate.getAllByCategoryService("OBLIGACION");
        ListDataProvider<BasicServiceProvider> dataProvider = new ListDataProvider<>(basicServiceProviderList);
        Grid<BasicServiceProvider> grid = new Grid<>();
        grid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_WRAP_CELL_CONTENT);
        grid.setWidthFull();
        grid.setDataProvider(dataProvider);

        grid.addColumn(BasicServiceProvider::getProvider)
                .setSortable(true)
                .setAutoWidth(true)
                .setFlexGrow(1)
                .setKey("name")
                .setHeader("Proveedor");
        grid.addColumn(BasicServiceProvider::getNit)
                .setSortable(true)
                .setAutoWidth(true)
                .setFlexGrow(1)
                .setKey("nit")
                .setHeader("NIT");
        grid.addColumn(BasicServiceProvider::getTypeService)
                .setSortable(true)
                .setAutoWidth(true)
                .setFlexGrow(1)
                .setKey("typeservice")
                .setHeader("Tipo Servicio");
        grid.addColumn(BasicServiceProvider::getDescription)
                .setSortable(true)
                .setAutoWidth(true)
                .setFlexGrow(1)
                .setKey("description")
                .setHeader("Descripción");
        grid.addColumn(new ComponentRenderer<>(this::createButtonSelectSupplier))
                .setFlexGrow(0)
                .setAutoWidth(true);

        HeaderRow hr = grid.appendHeaderRow();

        nameBasicProviderFilter = new TextField();
        nameBasicProviderFilter.setWidthFull();
        nameBasicProviderFilter.setValueChangeMode(ValueChangeMode.EAGER);
        nameBasicProviderFilter.addValueChangeListener(e -> applyFilterSupplier(dataProvider));
        hr.getCell(grid.getColumnByKey("name")).setComponent(nameBasicProviderFilter);


        nitBasicProviderFilter = new TextField();
        nitBasicProviderFilter.setWidthFull();
        nitBasicProviderFilter.setValueChangeMode(ValueChangeMode.EAGER);
        nitBasicProviderFilter.addValueChangeListener(e -> applyFilterSupplier(dataProvider));
        hr.getCell(grid.getColumnByKey("nit")).setComponent(nitBasicProviderFilter);

        descriptionBasicProviderFilter = new TextField();
        descriptionBasicProviderFilter.setWidthFull();
        descriptionBasicProviderFilter.setValueChangeMode(ValueChangeMode.EAGER);
        descriptionBasicProviderFilter.addValueChangeListener(e -> applyFilterSupplier(dataProvider));
        hr.getCell(grid.getColumnByKey("description")).setComponent(descriptionBasicProviderFilter);

        typeServiceBasicProviderFilter = new TextField();
        typeServiceBasicProviderFilter.setWidthFull();
        typeServiceBasicProviderFilter.setValueChangeMode(ValueChangeMode.EAGER);
        typeServiceBasicProviderFilter.addValueChangeListener(e -> applyFilterSupplier(dataProvider));
        hr.getCell(grid.getColumnByKey("typeservice")).setComponent(typeServiceBasicProviderFilter);

        return grid;
    }

    private Component createButtonSelectSupplier(BasicServiceProvider basicServiceProvider){
        Button btn = new Button();
        btn.setIcon(VaadinIcon.CHEVRON_CIRCLE_UP.create());
        btn.setEnabled(GrantOptions.grantedOptionWrite("Obligaciones"));
        btn.addClickListener(event -> {
            nameSupplier.setValue(basicServiceProvider.getProvider());
            basicServiceProviderSelected = basicServiceProvider;

            detailsDrawer.hide();
        });

        return btn;
    }

    private void applyFilterSupplier(ListDataProvider<BasicServiceProvider> dataProvider){
        dataProvider.clearFilters();
        if(!nameBasicProviderFilter.getValue().trim().equals("")){
            dataProvider.addFilter(supplier -> StringUtils.containsIgnoreCase(supplier.getProvider(), nameBasicProviderFilter.getValue()));
        }
        if(!nitBasicProviderFilter.getValue().trim().equals("")){
            dataProvider.addFilter(supplier -> Objects.equals(supplier.getNit(), nitBasicProviderFilter.getValue()));
        }
        if(!descriptionBasicProviderFilter.getValue().trim().equals("")){
            dataProvider.addFilter(supplier -> StringUtils.containsIgnoreCase(supplier.getDescription(), descriptionBasicProviderFilter.getValue()));
        }
        if(!typeServiceBasicProviderFilter.getValue().trim().equals("")){
            dataProvider.addFilter(supplier -> StringUtils.containsIgnoreCase(supplier.getTypeService(), typeServiceBasicProviderFilter.getValue()));
        }
    }

    //**********************************EXPENSE DITRIBUITE**************/
    private FormLayout layoutExpenseDistribuite(ExpenseDistribuite expenseDistribuite){

        IntegerField codeFatherBusinessUnit = new IntegerField();
        codeFatherBusinessUnit.setWidth("20%");
        codeFatherBusinessUnit.setReadOnly(true);

        IntegerField codeBusinessUnit = new IntegerField();
        codeBusinessUnit.setWidth("20%");
        codeBusinessUnit.setReadOnly(true);

        TextField nameBusinessUnit = new TextField();
        nameBusinessUnit.setWidth("70%");
        nameBusinessUnit.setReadOnly(true);

        NumberField amount = new NumberField();
        amount.setWidthFull();
        amount.setMin(0.0);
        amount.setRequiredIndicatorVisible(true);

        ComboBox<Concept> unitBusiness = new ComboBox<>();
        unitBusiness.setWidthFull();
        unitBusiness.setItems(conceptList);
        unitBusiness.setItemLabelGenerator(Concept::getDescription);
        unitBusiness.setRequiredIndicatorVisible(true);
        unitBusiness.setErrorMessage("Seleccione la unidad de negocios");
        if(expenseDistribuite.getCodeBusinessUnit()!=null){

            unitBusiness.setValue(conceptList.stream()
                    .filter(c -> c.getCode2()!=null && c.getCode2().equals(expenseDistribuite.getCodeBusinessUnit().toString()))
                    .findFirst().get());
        }
        unitBusiness.addValueChangeListener(event -> {
            if(event.getValue() != null) {
                Optional<ExpenseDistribuite> opt = expenseDistribuiteList.stream()
                        .filter(exp -> exp.getCodeBusinessUnit().equals(Integer.parseInt(event.getValue().getCode2())))
                        .findFirst();
                if (!opt.isPresent()) {
                    codeBusinessUnit.setValue(Integer.valueOf(event.getValue().getCode2()));
                    nameBusinessUnit.setValue(event.getValue().getDescription());
                    codeFatherBusinessUnit.setValue(Integer.valueOf(event.getValue().getCode()));
                } else {
                    UIUtils.showNotificationType("Unidad de Negocio ya fue agregada", "alert");
                    unitBusiness.clear();
                    codeBusinessUnit.clear();
                    nameBusinessUnit.clear();
                    codeFatherBusinessUnit.clear();
                }
            }
        });


        expenseDistribuiteBinder = new BeanValidationBinder<>(ExpenseDistribuite.class);
        expenseDistribuiteBinder.forField(codeFatherBusinessUnit)
                .asRequired("Código Unidad de Sucursal es requerido")
                .bind(ExpenseDistribuite::getCodeFatherBusinessUnit,ExpenseDistribuite::setCodeFatherBusinessUnit);
        expenseDistribuiteBinder.forField(codeBusinessUnit)
                .asRequired("Código Unidad negocio es requerido")
                .bind(ExpenseDistribuite::getCodeBusinessUnit,ExpenseDistribuite::setCodeBusinessUnit);
        expenseDistribuiteBinder.forField(nameBusinessUnit)
                .asRequired("Nombre unidad negocio es requerido")
                .withConverter(new UtilValues.StringTrimValue())
                .bind(ExpenseDistribuite::getNameBusinessUnit,ExpenseDistribuite::setNameBusinessUnit);
        expenseDistribuiteBinder.forField(amount)
                .asRequired("Monto es requerido")
                .withValidator(m -> m.doubleValue()>0.0,"Monto debe ser mayor a 0")
                .bind(ExpenseDistribuite::getAmount,ExpenseDistribuite::setAmount);

        expenseDistribuiteBinder.addStatusChangeListener(event -> {
            boolean isValid = !event.hasValidationErrors();
            boolean hasChanges = expenseDistribuiteBinder.hasChanges();
//            if(obligationsDto.getState()==null || !obligationsDto.getState().equals("FINALIZADO")){
//                footerExpenseDistribuite.saveState(hasChanges && isValid && GrantOptions.grantedOptionWrite("Obligaciones"));
//            }else{
//                footerExpenseDistribuite.saveState(false);
//            }
            if(VaadinSession.getCurrent().getAttribute("memberOf").toString().contains("BUDGET_ADMIN")){
                footer.saveState(isValid && hasChanges && GrantOptions.grantedOptionWrite("Servicios Básicos"));
            }else {
                footer.saveState(isValid && hasChanges && GrantOptions.grantedOptionWrite("Servicios Básicos")
                        && (obligationsDto.getState()==null || !obligationsDto.getState().equals("FINALIZADO")));
            }

        });

        expenseDistribuiteBinder.readBean(expenseDistribuite);

        FormLayout form = new FormLayout();
        form.addClassNames(LumoStyles.Padding.Bottom.L,
                LumoStyles.Padding.Horizontal.S, LumoStyles.Padding.Top.S);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1,
                        FormLayout.ResponsiveStep.LabelsPosition.TOP),
                new FormLayout.ResponsiveStep("21em", 2,
                        FormLayout.ResponsiveStep.LabelsPosition.TOP));

        FormLayout.FormItem unitBusinessItem = form.addFormItem(unitBusiness,"Unidad de Negocio");
        UIUtils.setColSpan(2,unitBusinessItem);

        FormLayout.FormItem amountItem = form.addFormItem(amount,"Monto (Bs.)");
        UIUtils.setColSpan(2,amountItem);

        return form;
    }

    private void showDetailsExpenseDistribuite(ExpenseDistribuite expenseDistribuite){
        setViewDetails(createDetailsDrawerExpenseDistribuite());
        setViewDetailsPosition(Position.RIGHT);
        currentExpenseDistribuite = expenseDistribuite;
        detailsDrawerHeaderExpenseDistribuite.setTitle("Unidad: "+expenseDistribuite.getNameBusinessUnit()==null?"Nueva":expenseDistribuite.getNameBusinessUnit());
        detailsDrawerExpenseDistribuite.setContent(layoutExpenseDistribuite(currentExpenseDistribuite));
        detailsDrawerExpenseDistribuite.show();
    }

    private DetailsDrawer createDetailsDrawerExpenseDistribuite(){
        detailsDrawerExpenseDistribuite = new DetailsDrawer(DetailsDrawer.Position.RIGHT);

        detailsDrawerHeaderExpenseDistribuite = new DetailsDrawerHeader("");
        detailsDrawerHeaderExpenseDistribuite.addCloseListener(event -> detailsDrawerExpenseDistribuite.hide());
        detailsDrawerExpenseDistribuite.setHeader(detailsDrawerHeaderExpenseDistribuite);

        footerExpenseDistribuite = new DetailsDrawerFooter();
        footerExpenseDistribuite.addSaveListener(e -> {
            if(currentExpenseDistribuite !=null && expenseDistribuiteBinder.writeBeanIfValid(currentExpenseDistribuite)){

                expenseDistribuiteList.removeIf(ed -> ed.getCodeBusinessUnit().equals(currentExpenseDistribuite.getCodeBusinessUnit()));
                expenseDistribuiteList.add(currentExpenseDistribuite);
                detailsDrawerExpenseDistribuite.hide();
                expenseDistribuiteGrid.getDataProvider().refreshAll();

//                if(obligationsDto.getState()==null || !obligationsDto.getState().equals("FINALIZADO")) {
//                    footer.saveState(GrantOptions.grantedOptionWrite("Obligaciones") );
//                }else{
//                    footer.saveState(false);
//                }
                if(VaadinSession.getCurrent().getAttribute("memberOf").toString().contains("BUDGET_ADMIN")){
                    footer.saveState(GrantOptions.grantedOptionWrite("Servicios Básicos"));
                }else {
                    footer.saveState(GrantOptions.grantedOptionWrite("Servicios Básicos")
                            && (obligationsDto.getState()==null || !obligationsDto.getState().equals("FINALIZADO")));
                }

            }
        });
        footerExpenseDistribuite.addCancelListener(e -> detailsDrawerExpenseDistribuite.hide());
        detailsDrawerExpenseDistribuite.setFooter(footerExpenseDistribuite);

        return detailsDrawerExpenseDistribuite;
    }

    private VerticalLayout gridExpenseDistribuite(){

        VerticalLayout layout = new VerticalLayout();
        layout.setWidthFull();
        Button btnAdd = new Button("Adicionar");
        btnAdd.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_CONTRAST,ButtonVariant.LUMO_SMALL);
        btnAdd.addClickListener(event -> {
//            setViewContent(layoutExpenseDistribuite(currentExpenseDistribuite));
            setViewDetailsPosition(Position.RIGHT);
            setViewDetails(createDetailsDrawerExpenseDistribuite());
            showDetailsExpenseDistribuite(new ExpenseDistribuite());
        });


        expenseDistribuiteGrid = new Grid<>();
        expenseDistribuiteGrid.setWidthFull();
        expenseDistribuiteGrid.setDataProvider(expenseDistribuiteDataProvider);
        expenseDistribuiteGrid.addColumn(ExpenseDistribuite::getNameBusinessUnit)
                .setSortable(true)
                .setAutoWidth(true)
                .setResizable(true)
                .setFlexGrow(0)
                .setHeader("Unidad de Negocio");
        expenseDistribuiteGrid.addColumn(new NumberRenderer<>(ExpenseDistribuite::getAmount, " %(,.2f",
                        Locale.US, "0.00"))
                .setSortable(true)
                .setAutoWidth(true)
                .setResizable(true)
                .setFlexGrow(0)
                .setHeader("Monto(Bs.)");
        expenseDistribuiteGrid.addColumn(new ComponentRenderer<>(this::createButtonDelete))
                .setFlexGrow(0)
                .setAutoWidth(true);
        expenseDistribuiteGrid.addColumn(new ComponentRenderer<>(this::createButtonEdit))
                .setFlexGrow(0)
                .setAutoWidth(true);

        layout.add(btnAdd,expenseDistribuiteGrid);
        layout.setHorizontalComponentAlignment(FlexComponent.Alignment.END,btnAdd);

        return layout;
    }

    private Component createButtonDelete(ExpenseDistribuite expenseDistribuite){
        Button btn = new Button();
        btn.setIcon(VaadinIcon.TRASH.create());
        btn.addThemeVariants(ButtonVariant.LUMO_ERROR,ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_SMALL);
        btn.setEnabled(GrantOptions.grantedOptionWrite("Obligaciones"));
        Tooltips.getCurrent().setTooltip(btn,"Eliminar");
        btn.addClickListener(event -> {

            SweetAlert2Vaadin sweetAlert2Vaadin = new DialogSweetAlert().dialogConfirm("Eliminar Registro",
                    "¿Deseas Eliminar la Distribución? ");

            sweetAlert2Vaadin.open();
            sweetAlert2Vaadin.addConfirmListener(e -> {
                expenseDistribuiteList.remove(expenseDistribuite);
                expenseDistribuiteGrid.getDataProvider().refreshAll();
//                if(obligationsDto.getState()==null || !obligationsDto.getState().equals("FINALIZADO")) {
//                    footer.saveState(GrantOptions.grantedOptionWrite("Obligaciones"));
//                }else{
//                    footer.saveState(false);
//                }
                if(VaadinSession.getCurrent().getAttribute("memberOf").toString().contains("BUDGET_ADMIN")){
                    footer.saveState(GrantOptions.grantedOptionWrite("Obligaciones"));
                }else {
                    footer.saveState(GrantOptions.grantedOptionWrite("Obligaciones")
                            && (obligationsDto.getState()==null || !obligationsDto.getState().equals("FINALIZADO")));
                }
            });
            sweetAlert2Vaadin.addCancelListener(e -> e.getSource().close());



        });

        return btn;
    }

    private Component createButtonEdit(ExpenseDistribuite expenseDistribuite){
        Button btn = new Button();
        btn.setIcon(VaadinIcon.EDIT.create());
        btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btn.setEnabled(GrantOptions.grantedOptionRead("Obligaciones"));
        Tooltips.getCurrent().setTooltip(btn,"Editar");
        btn.addClickListener(event -> {
            setViewDetailsPosition(Position.RIGHT);
            setViewDetails(createDetailsDrawerExpenseDistribuite());
            showDetailsExpenseDistribuite(expenseDistribuite);
        });
        return btn;
    }

    private boolean validateAmountExpenseDistribuite(){
        Double result = expenseDistribuiteList.stream()
                .mapToDouble(e -> e.getAmount()).sum();
        return (Math.round(result.doubleValue()*100.0)/100.0 == obligationsDto.getAmount().doubleValue());
    }

    private Obligations fillObligations() throws JsonProcessingException {
        Obligations obligations = new Obligations();
        if(obligationsDto.getId()==null){
            obligations.setCreatedBy(VaadinSession.getCurrent().getAttribute("login").toString());
            obligations.setState("INICIADO");
        }else{
            obligations.setCreatedBy(obligationsDto.getCreatedBy());
            obligations.setState(obligationsDto.getState());
        }
        obligations.setId(obligationsDto.getId());
        obligations.setTypeObligation(obligationsDto.getTypeObligation());
        obligations.setIdSupplier(obligationsDto.getIdSupplier());
        obligations.setDescription(obligationsDto.getDescription());
        obligations.setPeriod(obligationsDto.getPeriod());
        obligations.setPaymentDate(obligationsDto.getPaymentDate());
        obligations.setAmount(obligationsDto.getAmount());
        obligations.setAccount(obligationsDto.getAccount());
        obligations.setSubAccount(obligationsDto.getSubAccount());
        String json = mapper.writeValueAsString(expenseDistribuiteList);
        obligations.setExpenseDistribuite(json);
        obligations.setTypeDocumentReceived(obligationsDto.getTypeDocumentReceived());
        obligations.setNumberDocumentReceived(obligationsDto.getNumberDocumentReceived());
        String jsonInvoiceAuthorizer = mapper.writeValueAsString(selectedInvoiceAuthorizerList);
        obligations.setInvoiceAuthorizer(jsonInvoiceAuthorizer);
        obligations.setDateDeliveryAccounting(obligationsDto.getDateDeliveryAccounting());
        obligations.setAccountingPerson(obligationsDto.getAccountingPerson());
        return obligations;
    }
    // Invoice Authorizer

    private void showDetailsInvoiceAuthorizer(SelectedInvoiceAuthorizer selectedInvoiceAuthorizer){
        setViewDetails(createDetailsDrawerSelectedInvoiceAuthorizer());
        setViewDetailsPosition(Position.RIGHT);
        currentSelectedInvoiceAuthorizer = selectedInvoiceAuthorizer;
        detailsDrawerHeaderSelectedInvoiceAuthorizer.setTitle("Autorizador: "
                .concat(selectedInvoiceAuthorizer.getFullName()==null?"Nuevo Autorizador":selectedInvoiceAuthorizer.getFullName()));
        detailsDrawerSelectedInvoiceAuthorizer.setContent(layoutInvoiceAuthorizers(currentSelectedInvoiceAuthorizer));
        detailsDrawerSelectedInvoiceAuthorizer.show();
    }

    private DetailsDrawer createDetailsDrawerSelectedInvoiceAuthorizer(){

        detailsDrawerSelectedInvoiceAuthorizer = new DetailsDrawer(DetailsDrawer.Position.RIGHT);
        detailsDrawerHeaderSelectedInvoiceAuthorizer = new DetailsDrawerHeader("");
        detailsDrawerHeaderSelectedInvoiceAuthorizer.addCloseListener(event -> detailsDrawerSelectedInvoiceAuthorizer.hide());
        detailsDrawerSelectedInvoiceAuthorizer.setHeader(detailsDrawerHeaderSelectedInvoiceAuthorizer);

        footerInvoiceAuthorizer = new DetailsDrawerFooter();
        footerInvoiceAuthorizer.addSaveListener(e -> {
            if(currentSelectedInvoiceAuthorizer != null && selectedInvoiceAuthorizerBinder.writeBeanIfValid(currentSelectedInvoiceAuthorizer)){
                selectedInvoiceAuthorizerList.removeIf(sa -> sa.getId().equals(currentSelectedInvoiceAuthorizer.getId()));
                currentSelectedInvoiceAuthorizer.setId(UUID.randomUUID());
                currentSelectedInvoiceAuthorizer.setRegisterDate(LocalDate.now());
                selectedInvoiceAuthorizerList.add(currentSelectedInvoiceAuthorizer);
                detailsDrawerSelectedInvoiceAuthorizer.hide();
                selectedInvoiceAuthorizerGrid.getDataProvider().refreshAll();
                footerInvoiceAuthorizer.saveState(GrantOptions.grantedOptionWrite("Obligaciones"));
//                if(obligationsDto.getState()==null || !obligationsDto.getState().equals("FINALIZADO")) {
//                    footer.saveState(GrantOptions.grantedOptionWrite("Obligaciones"));
//                }else{
//                    footer.saveState(false);
//                }

                if(VaadinSession.getCurrent().getAttribute("memberOf").toString().contains("BUDGET_ADMIN")){
                    footer.saveState(GrantOptions.grantedOptionWrite("Obligaciones"));
                }else {
                    footer.saveState(GrantOptions.grantedOptionWrite("Obligaciones")
                            && (obligationsDto.getState()==null || !obligationsDto.getState().equals("FINALIZADO")));
                }
            }
        });

        footerInvoiceAuthorizer.addCancelListener(e -> detailsDrawerSelectedInvoiceAuthorizer.hide());
        detailsDrawerSelectedInvoiceAuthorizer.setFooter(footerInvoiceAuthorizer);

        return detailsDrawerSelectedInvoiceAuthorizer;

    }

    private FormLayout layoutInvoiceAuthorizers(SelectedInvoiceAuthorizer selectedInvoiceAuthorizer){

        Concept concept = conceptList.stream()
                .filter(c -> String.valueOf(expenseDistribuiteList.get(0).getCodeBusinessUnit()).equals(c.getCode2()))
                .findFirst().get();
//        List<InvoiceAuthorizer> invoiceAuthorizerList = invoiceAuthorizerTemplate
//                .getByCodeBranchOffice(Integer.valueOf(concept.getCode()));
        List<InvoiceAuthorizer> invoiceAuthorizerList = invoiceAuthorizerTemplate
                .getAll();

        TextField codePosition = new TextField();
        codePosition.setWidthFull();
        codePosition.setRequired(true);
        codePosition.setReadOnly(true);

        TextField priorityLevel = new TextField();
        priorityLevel.setRequired(true);
        priorityLevel.setReadOnly(true);
        priorityLevel.setWidthFull();

        TextField nameBranchOffice = new TextField();
        nameBranchOffice.setWidthFull();
        nameBranchOffice.setRequired(true);
        nameBranchOffice.setReadOnly(true);

        ComboBox<String> fullName = new ComboBox<>();
        fullName.setRequired(true);
        fullName.setWidthFull();
        fullName.setAllowCustomValue(false);
        fullName.setItems(invoiceAuthorizerList.stream()
                .map(InvoiceAuthorizer::getFullName)
                .collect(Collectors.toList()));
        fullName.addValueChangeListener(event -> {
            InvoiceAuthorizer invoiceAuthorizer = invoiceAuthorizerList.stream()
                    .filter(a -> event.getValue().equals(a.getFullName()))
                    .findFirst().get();
            codePosition.setValue(invoiceAuthorizer.getCodePosition());
            nameBranchOffice.setValue(invoiceAuthorizer.getNameBranchOffice());
            priorityLevel.setValue(invoiceAuthorizer.getPriorityLevel());
        });

        selectedInvoiceAuthorizerBinder = new BeanValidationBinder<>(SelectedInvoiceAuthorizer.class);
        selectedInvoiceAuthorizerBinder.forField(fullName)
                .asRequired("Autorizador es requerido")
                .bind(SelectedInvoiceAuthorizer::getFullName,SelectedInvoiceAuthorizer::setFullName);
        selectedInvoiceAuthorizerBinder.forField(codePosition)
                .asRequired("Código Cargo es requerido")
                .bind(SelectedInvoiceAuthorizer::getCodePosition,SelectedInvoiceAuthorizer::setCodePosition);
        selectedInvoiceAuthorizerBinder.forField(nameBranchOffice)
                .asRequired("Unidad Negocio es requerida")
                .withConverter(new UtilValues.StringTrimValue())
                .bind(SelectedInvoiceAuthorizer::getNameBranchOffice,SelectedInvoiceAuthorizer::setNameBranchOffice);
        selectedInvoiceAuthorizerBinder.forField(priorityLevel)
                .asRequired("Nivel Autorización es requerido")
                .withConverter(new UtilValues.StringTrimValue())
                .bind(SelectedInvoiceAuthorizer::getPriorityLevel,SelectedInvoiceAuthorizer::setPriorityLevel);

        FormLayout form = new FormLayout();
        form.addClassNames(LumoStyles.Padding.Bottom.L,
                LumoStyles.Padding.Horizontal.S, LumoStyles.Padding.Top.S);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1,
                        FormLayout.ResponsiveStep.LabelsPosition.TOP),
                new FormLayout.ResponsiveStep("21em", 2,
                        FormLayout.ResponsiveStep.LabelsPosition.TOP));

        FormLayout.FormItem fullNameItem = form.addFormItem(fullName,"Autorizador");
        UIUtils.setColSpan(2,fullNameItem);
        form.addFormItem(codePosition,"Código Cargo");
        form.addFormItem(priorityLevel,"Nivel Autorización");
        FormLayout.FormItem nameBranchOfficeItem = form.addFormItem(nameBranchOffice,"Unidad Negocio");
        UIUtils.setColSpan(2,nameBranchOfficeItem);

        return form;
    }

    private DetailsDrawer createGridSelectedInvoiceAuthorizer(){

        VerticalLayout layout = new VerticalLayout();
        layout.setWidthFull();
        Button btnAdd = new Button("Adicionar");
        btnAdd.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_CONTRAST,ButtonVariant.LUMO_SMALL);
        btnAdd.setEnabled(GrantOptions.grantedOptionWrite("Obligaciones"));
        btnAdd.addClickListener(event -> {
            setViewDetailsPosition(Position.RIGHT);
            setViewDetails(createDetailsDrawerSelectedInvoiceAuthorizer());
            showDetailsInvoiceAuthorizer(new SelectedInvoiceAuthorizer());
        });

        selectedInvoiceAuthorizerGrid = new Grid<>();
        selectedInvoiceAuthorizerGrid.setWidthFull();
        selectedInvoiceAuthorizerGrid.setDataProvider(selectedInvoiceAuthorizerDataProvider);

        selectedInvoiceAuthorizerGrid.addColumn(SelectedInvoiceAuthorizer::getFullName)
                .setSortable(true)
                .setAutoWidth(true)
                .setResizable(true)
                .setFlexGrow(1)
                .setHeader("Autorizador");
        selectedInvoiceAuthorizerGrid.addColumn(SelectedInvoiceAuthorizer::getCodePosition)
                .setSortable(true)
                .setAutoWidth(true)
                .setResizable(true)
                .setFlexGrow(1)
                .setHeader("Cargo");
        selectedInvoiceAuthorizerGrid.addColumn(SelectedInvoiceAuthorizer::getNameBranchOffice)
                .setSortable(true)
                .setAutoWidth(true)
                .setResizable(true)
                .setFlexGrow(1)
                .setHeader("Unidad Negocio");
        selectedInvoiceAuthorizerGrid.addColumn(new ComponentRenderer<>(this::createButtonDeleteSelectedInvoiceAuthorizer))
                .setFlexGrow(1)
                .setAutoWidth(true);

        layout.add(btnAdd,selectedInvoiceAuthorizerGrid);
        layout.setHorizontalComponentAlignment(FlexComponent.Alignment.END,btnAdd);

        DetailsDrawer detailsDrawer = new DetailsDrawer(DetailsDrawer.Position.BOTTOM);
        detailsDrawer.setWidthFull();
        detailsDrawer.setHeight("90%");

        detailsDrawer.setPadding(Left.S, Right.S, Top.S);
        detailsDrawer.setContent(layout);
//        detailsDrawer.setFooter(footer);
        detailsDrawer.show();

        return detailsDrawer;
    }

    private Component createButtonDeleteSelectedInvoiceAuthorizer(SelectedInvoiceAuthorizer selectedInvoiceAuthorizer){
        Button btn = new Button();
        btn.setIcon(VaadinIcon.TRASH.create());
        btn.addThemeVariants(ButtonVariant.LUMO_ERROR,ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_SMALL);
        btn.setEnabled(GrantOptions.grantedOptionWrite("Obligaciones"));
        Tooltips.getCurrent().setTooltip(btn,"Eliminar");
        btn.addClickListener(event -> {

            SweetAlert2Vaadin sweetAlert2Vaadin = new DialogSweetAlert().dialogConfirm("Eliminar Registro",
                    "¿Deseas Eliminar al Autorizador? ");

            sweetAlert2Vaadin.open();
            sweetAlert2Vaadin.addConfirmListener(e -> {
                selectedInvoiceAuthorizerList.remove(selectedInvoiceAuthorizer);
                selectedInvoiceAuthorizerGrid.getDataProvider().refreshAll();
            });
            sweetAlert2Vaadin.addCancelListener(e -> e.getSource().close());

        });

        return btn;
    }

    //  DELIVER ACCOUNTING

    private DetailsDrawer createDeliverAccounting(){

        dateDeliveryAccounting = new DatePicker("Fecha de Entrega a Contabilidad");
        dateDeliveryAccounting.setWidth("30%");
        dateDeliveryAccounting.setRequired(true);
        dateDeliveryAccounting.setClearButtonVisible(true);
        dateDeliveryAccounting.setRequiredIndicatorVisible(true);
        dateDeliveryAccounting.setLocale(new Locale("es","BO"));
        dateDeliveryAccounting.setI18n(UIUtils.spanish());

        accountingPerson = new ComboBox<>("Entregado a");
        accountingPerson.setWidth("30%");
        accountingPerson.setRequired(true);
        accountingPerson.setRequiredIndicatorVisible(true);
        List<Parameter> parameterList = parameterRestTemplate.getByCategory("CODIGO CARGOS");

        String position = parameterList.stream()
                .filter(p -> p.getValue().equals("CONTABILIDAD"))
                .map(Parameter::getDetails)
                .findFirst().get();
        List<String> nameUsersPosition =  utilValues.getNameUserLdapByCriteria("title",position);
        accountingPerson.setItems(nameUsersPosition);


//        if(current.getExpenseDistribuite()!=null && !current.getExpenseDistribuite().equals("[]")) {
        binder.forField(dateDeliveryAccounting)
//                    .asRequired("Fecha entrega a contabilidad es requerida")
                .bind(ObligationsDto::getDateDeliveryAccounting, ObligationsDto::setDateDeliveryAccounting);
        binder.forField(accountingPerson)
//                    .asRequired("Responsable de contabilidad es requerido")
                .bind(ObligationsDto::getAccountingPerson, ObligationsDto::setAccountingPerson);

        binder.addStatusChangeListener(event -> {
            boolean isValid = !event.hasValidationErrors();
            boolean hasChanges = binder.hasChanges();
//            if(obligationsDto.getState()==null || !obligationsDto.getState().equals("FINALIZADO")) {
//                footer.saveState(isValid && hasChanges && GrantOptions.grantedOptionWrite("Obligaciones"));
//            }else{
//                footer.saveState(false);
////            footer.saveState(isValid && hasChanges && GrantOptions.grantedOptionWrite("Obligaciones")
////                    && !obligationsDto.getState().equals("FINALIZADO")) ;
//            }

            if(VaadinSession.getCurrent().getAttribute("memberOf").toString().contains("BUDGET_ADMIN")){
                footer.saveState(GrantOptions.grantedOptionWrite("Obligaciones"));
            }else {
                footer.saveState(GrantOptions.grantedOptionWrite("Obligaciones")
                        && (obligationsDto.getState()==null || !obligationsDto.getState().equals("FINALIZADO")));
            }
        });
//        }
        VerticalLayout layout = new VerticalLayout();
        layout.setWidthFull();
        layout.add(dateDeliveryAccounting,accountingPerson);
        layout.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER,dateDeliveryAccounting,
                accountingPerson);

        DetailsDrawer detailsDrawer = new DetailsDrawer(DetailsDrawer.Position.BOTTOM);
        detailsDrawer.setWidthFull();
        detailsDrawer.setHeight("90%");

        detailsDrawer.setPadding(Left.S, Right.S, Top.S);
        detailsDrawer.setContent(layout);
//        detailsDrawer.setFooter(footer);
        detailsDrawer.show();


        return detailsDrawer;
    }
}
