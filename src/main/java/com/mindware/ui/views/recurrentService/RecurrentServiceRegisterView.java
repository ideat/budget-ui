package com.mindware.ui.views.recurrentService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindware.backend.entity.basicServices.BasicServicesDto;
import com.mindware.backend.entity.config.Parameter;
import com.mindware.backend.entity.contract.Contract;
import com.mindware.backend.entity.corebank.Concept;
import com.mindware.backend.entity.commonJson.ExpenseDistribuite;
import com.mindware.backend.entity.invoiceAuthorizer.InvoiceAuthorizer;
import com.mindware.backend.entity.invoiceAuthorizer.SelectedInvoiceAuthorizer;
import com.mindware.backend.entity.recurrentService.RecurrentService;
import com.mindware.backend.entity.recurrentService.RecurrentServiceDto;
import com.mindware.backend.entity.supplier.Supplier;
import com.mindware.backend.rest.contract.ContractRestTemplate;
import com.mindware.backend.rest.corebank.ConceptRestTemplate;
import com.mindware.backend.rest.invoiceAuthorizer.InvoiceAuthorizerRestTemplate;
import com.mindware.backend.rest.parameter.ParameterRestTemplate;
import com.mindware.backend.rest.recurrentService.RecurrentServiceDtoRestTemplate;
import com.mindware.backend.rest.recurrentService.RecurrentServiceRestTemplate;
import com.mindware.backend.rest.supplier.SupplierRestTemplate;
import com.mindware.backend.util.GrantOptions;
import com.mindware.backend.util.UtilValues;
import com.mindware.ui.MainLayout;
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
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
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
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinSession;
import dev.mett.vaadin.tooltip.Tooltips;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Route(value = "recurrent-service-register", layout = MainLayout.class)
@ParentLayout(MainLayout.class)
@PageTitle("Registro Pago Servicios Recurrentes")
public class RecurrentServiceRegisterView extends SplitViewFrame implements HasUrlParameter<String>, RouterLayout {

    @Autowired
    private RecurrentServiceRestTemplate recurrentServiceRestTemplate;

    @Autowired
    private RecurrentServiceDtoRestTemplate recurrentServiceDtoRestTemplate;

    @Autowired
    private SupplierRestTemplate supplierRestTemplate;

    @Autowired
    private UtilValues utilValues;

    @Autowired
    private ContractRestTemplate contractRestTemplate;

    @Autowired
    private ConceptRestTemplate conceptRestTemplate;

    @Autowired
    private InvoiceAuthorizerRestTemplate invoiceAuthorizerTemplate;

    @Autowired
    private ParameterRestTemplate parameterRestTemplate;

    private Supplier supplierSelected;

    private ListDataProvider<ExpenseDistribuite> expenseDistribuiteDataProvider;
    private Binder<RecurrentServiceDto> binder;
    private Binder<ExpenseDistribuite> expenseDistribuiteBinder;
    private ExpenseDistribuite currentExpenseDistribuite;

    private ObjectMapper mapper;

    private DetailsDrawerFooter footer;
    private DetailsDrawerFooter footerExpenseDistribuite;
    private DetailsDrawer detailsDrawer;
    private DetailsDrawerHeader detailsDrawerHeader;
    private DetailsDrawer detailsDrawerExpenseDistribuite;
    private DetailsDrawerHeader detailsDrawerHeaderExpenseDistribuite;
    private String title;

    private List<ExpenseDistribuite> expenseDistribuiteList;

    private RecurrentServiceDto recurrentServiceDto;

    private TextField supplierName;
    private ComboBox<String> account;
    private ComboBox<String> subAccount;
    private TextField nitSupplier;
    private IntegerField contract;
    private  TextField paymentFrecuency;
    private DatePicker paymentDate;

    private Grid<ExpenseDistribuite> expenseDistribuiteGrid;

    private TextField supplierNameFilter;
    private TextField nitSupplierFilter;
    private TextField locationSupplierFilter;
    private TextField primaryActivitySupplierFilter;

    private TextField numberContractFilter;
    private TextField objectContractFilter;

    private DatePicker dateDeliveryAccounting;
    private ComboBox<String> accountingPerson;

    private DatePicker finishDate;
    private  Checkbox tacitReductionClause;

    private List<Concept> conceptList;

    private Map<String, List<String>> param;

    private FlexBoxLayout contentCreateRecurrentService;
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


    @Override
    public void setParameter(BeforeEvent beforeEvent, @OptionalParameter String s) {
        mapper = new ObjectMapper();
        Location location = beforeEvent.getLocation();
        QueryParameters queryParameters = location.getQueryParameters();
        param = queryParameters.getParameters();
        footer = new DetailsDrawerFooter();

        if(!param.get("id").get(0).equals("NUEVO")){
            recurrentServiceDto = recurrentServiceDtoRestTemplate.getById(param.get("id").get(0));
            title = "Proveedor: ".concat(recurrentServiceDto.getSupplierName());
            supplierSelected = supplierRestTemplate.getById(recurrentServiceDto.getIdSupplier().toString());


        }else{
            recurrentServiceDto = new RecurrentServiceDto();
            recurrentServiceDto.setExpenseDistribuite("[]");
            recurrentServiceDto.setInvoiceAuthorizer("[]");
            title = "Registro Nuevo ";
        }

        try {
            expenseDistribuiteList = mapper.readValue(recurrentServiceDto.getExpenseDistribuite(), new TypeReference<List<ExpenseDistribuite>>() {});
            selectedInvoiceAuthorizerList = mapper.readValue(recurrentServiceDto.getInvoiceAuthorizer(), new TypeReference<List<SelectedInvoiceAuthorizer>>(){});
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        expenseDistribuiteDataProvider = new ListDataProvider<>(expenseDistribuiteList);
        selectedInvoiceAuthorizerDataProvider = new ListDataProvider<>(selectedInvoiceAuthorizerList);


        conceptList = new ArrayList<>(conceptRestTemplate.getAgencia());
//        conceptList.addAll(conceptRestTemplate.getSucursal());
        conceptList.sort(Comparator.comparing(Concept::getCode));

        contentCreateRecurrentService = (FlexBoxLayout) createContent(createRecurrentServiceDtoForm(recurrentServiceDto));
        contentInvoiceAuthorizer = (FlexBoxLayout) createContent(createGridSelectedInvoiceAuthorizer());
        contentDeliveyAccounting = (FlexBoxLayout) createContent(createDeliverAccounting());

        setViewDetails(createDetailDrawer());
        setViewDetailsPosition(Position.BOTTOM);


        footer.addSaveListener(event ->{
            if(recurrentServiceDto.getInvoiceAuthorizer()!=null && !recurrentServiceDto.getInvoiceAuthorizer().equals("[]") && GrantOptions.grantedOptionAccounting("Servicios Recurrentes")){
                binder.forField(dateDeliveryAccounting)
                        .asRequired("Fecha entrega a contabilidad es requerida")
                        .withValidator(d -> d.isAfter(paymentDate.getValue()),"Fecha entrega contabilidad no puede ser anterior a la fecha de pago")
                        .bind(RecurrentServiceDto::getDateDeliveryAccounting, RecurrentServiceDto::setDateDeliveryAccounting);
                binder.forField(accountingPerson)
                        .asRequired("Responsable de contabilidad es requerido")
                        .bind(RecurrentServiceDto::getAccountingPerson, RecurrentServiceDto::setAccountingPerson);

            }
            if(binder.writeBeanIfValid(recurrentServiceDto)){

                if(validateAmountExpenseDistribuite()) {
                    recurrentServiceDto.setIdSupplier(supplierSelected.getId());
                    try {
                        if(recurrentServiceDto.getId()==null){
                            recurrentServiceDto.setState("INICIADO");
                        }
                        RecurrentService recurrentService = recurrentServiceRestTemplate.add(fillRecurrentService());
                        recurrentServiceDto.setId(recurrentService.getId());
                        recurrentServiceDto.setInvoiceAuthorizer(recurrentService.getInvoiceAuthorizer());

                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
//                    UI.getCurrent().navigate(RecurrentServiceView.class);
                    UIUtils.showNotificationType("Datos registratos", "success");
                }else{
                    UIUtils.showNotificationType("Monto distribuido no cuadra con el monto del contrato","alert");
                }
            }
        });
        footer.addCancelListener(event -> UI.getCurrent().navigate(RecurrentServiceView.class));
        setViewFooter(footer);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent){
        super.onAttach(attachEvent);
        initBar();
        setViewContent(contentCreateRecurrentService,contentInvoiceAuthorizer,contentDeliveyAccounting);
        binder.readBean(recurrentServiceDto);
    }

    private AppBar initBar(){
        MainLayout.get().getAppBar().reset();
        AppBar appBar = MainLayout.get().getAppBar();

        appBar.addTab("Registro Factura Servicio Recurrente");
        appBar.addTab("Autorización Factura");
        if(GrantOptions.grantedOptionAccounting("Servicios Recurrentes"))
            appBar.addTab("Entrega a Contabilidad");
        appBar.centerTabs();

        currentTab = "Registro Factura Servicio Recurrente";
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
        appBar.getContextIcon().addClickListener(e -> UI.getCurrent().navigate("recurrent-service"));

        return appBar;
    }

    private void enabledSheets(){
        if(recurrentServiceDto.getId()==null){
            contentInvoiceAuthorizer.setEnabled(false);
        }else{
            contentInvoiceAuthorizer.setEnabled(true);
        }
        if(recurrentServiceDto.getInvoiceAuthorizer()==null || recurrentServiceDto.getInvoiceAuthorizer().equals("[]")){
            contentDeliveyAccounting.setEnabled(false);
        }else{
            contentDeliveyAccounting.setEnabled(true);
        }
    }

    private void hideContent(){
        contentCreateRecurrentService.setVisible(true);
        contentInvoiceAuthorizer.setVisible(false);
        contentDeliveyAccounting.setVisible(false);
        if(currentTab.equals("Registro Factura Servicio Recurrente")){
            contentCreateRecurrentService.setVisible(true);
            contentInvoiceAuthorizer.setVisible(false);
            contentDeliveyAccounting.setVisible(false);
        }else if(currentTab.equals("Autorización Factura")){
            contentCreateRecurrentService.setVisible(false);
            contentInvoiceAuthorizer.setVisible(true);
            contentDeliveyAccounting.setVisible(false);
        }else if(currentTab.equals("Entrega a Contabilidad")){
            contentCreateRecurrentService.setVisible(false);
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

    private DetailsDrawer createRecurrentServiceDtoForm(RecurrentServiceDto recurrentServiceDto){

        ComboBox<String> typeService = new ComboBox<>();
        typeService.setWidthFull();
        typeService.setItems(utilValues.getValueParameterByCategory("SERVICIOS RECURRENTES"));

        supplierName = new TextField();
        supplierName.setWidthFull();
        supplierName.setRequired(true);
        supplierName.setReadOnly(true);
        supplierName.addValueChangeListener(event -> {
            if(!event.getOldValue().equals(event.getValue())) {
                contract.clear();
                finishDate.clear();
                tacitReductionClause.clear();
                paymentFrecuency.clear();
            }
        });

        nitSupplier = new TextField();
        nitSupplier.setWidthFull();
        nitSupplier.setReadOnly(true);

        TextField description = new TextField();
        description.setWidthFull();
        description.setRequired(true);

        ComboBox<String> period = new ComboBox<>();
        period.setWidthFull();
        period.setItems(utilValues.generatePeriods());
        period.setRequired(true);
        period.setAllowCustomValue(true);

        paymentDate = new DatePicker();
        paymentDate.setWidthFull();
        paymentDate.setRequired(true);
        paymentDate.setLocale(new Locale("es","BO"));


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
        typeDocumentReceived.setItems("FACTURA","RECIBO");
        typeDocumentReceived.setRequired(true);

        IntegerField numberDocumentReceived = new IntegerField();
        numberDocumentReceived.setWidthFull();
        numberDocumentReceived.setRequiredIndicatorVisible(true);
        numberDocumentReceived.setMin(0);

        contract = new IntegerField();
        contract.setWidthFull();
        contract.setReadOnly(true);
        contract.setRequiredIndicatorVisible(true);

        finishDate = new DatePicker();
        finishDate.setWidthFull();
        finishDate.setReadOnly(true);
        finishDate.setLocale(new Locale("es","BO"));

        tacitReductionClause = new Checkbox("Clausula tácita reconducción");
        tacitReductionClause.setReadOnly(true);

        paymentFrecuency = new TextField();
        paymentFrecuency.setWidthFull();
        paymentFrecuency.setReadOnly(true);

        binder = new BeanValidationBinder<>(RecurrentServiceDto.class);
        binder.forField(typeService)
                .asRequired("Tipo de servicio es requerido")
                .bind(RecurrentServiceDto::getTypeService, RecurrentServiceDto::setTypeService);
        binder.forField(supplierName)
                .asRequired("Proveedor es requerido")
                .bind(RecurrentServiceDto::getSupplierName, RecurrentServiceDto::setSupplierName);
        binder.forField(nitSupplier)
                .asRequired("Numero NIT es requerido")
                .bind(RecurrentServiceDto::getSupplierNit, RecurrentServiceDto::setSupplierNit);
        binder.forField(description)
                .asRequired("Descripcion es requerida")
                .bind(RecurrentServiceDto::getDescription,RecurrentServiceDto::setDescription);
        binder.forField(period)
                .asRequired("Periodo es requerido")
                .bind(RecurrentServiceDto::getPeriod,RecurrentServiceDto::setPeriod);
        binder.forField(paymentDate)
                .asRequired("Fecha de pago es requerida")
                .bind(RecurrentServiceDto::getPaymentDate,RecurrentServiceDto::setPaymentDate);
        binder.forField(amount)
                .asRequired("Monto es requerido")
                .withValidator(m -> m.doubleValue()>0.0,"Monto debe ser mayor a 0")
                .bind(RecurrentServiceDto::getAmount,RecurrentServiceDto::setAmount);
        binder.forField(account)
                .asRequired("Cuenta es requerida")
                .bind(RecurrentServiceDto::getAccount,RecurrentServiceDto::setAccount);
        binder.forField(subAccount)
                .asRequired("Subcuenta es requerida")
                .bind(RecurrentServiceDto::getSubAccount,RecurrentServiceDto::setSubAccount);
        binder.forField(typeDocumentReceived)
                .asRequired("Tipo de documento es requerido")
                .bind(RecurrentServiceDto::getTypeDocumentReceived,RecurrentServiceDto::setTypeDocumentReceived);
        binder.forField(numberDocumentReceived)
                .asRequired("N° Factura/Recibo/CAABS es requerido")
                .bind(RecurrentServiceDto::getNumberDocumentReceived,RecurrentServiceDto::setNumberDocumentReceived);
        binder.forField(contract)
                .asRequired("N° contrato es requerido")
                .bind(RecurrentServiceDto::getNumberContract,RecurrentServiceDto::setNumberContract);
        binder.forField(finishDate)
//                .asRequired("Fecha vigencia contrato es requerido")
                .bind(RecurrentServiceDto::getFinishDate,RecurrentServiceDto::setFinishDate);
        binder.forField(tacitReductionClause)
                .bind(RecurrentServiceDto::getTacitReductionClause,RecurrentServiceDto::setTacitReductionClause);
        binder.forField(paymentFrecuency)
                .bind(RecurrentServiceDto::getPaymentFrecuency,RecurrentServiceDto::setPaymentFrecuency);

        binder.addStatusChangeListener(event -> {
           boolean isValid = !event.hasValidationErrors();
           boolean hasChanges = binder.hasChanges();
           footer.saveState(isValid && hasChanges && GrantOptions.grantedOptionWrite("Servicios Recurrentes")
           && !recurrentServiceDto.getState().equals("FINALIZADO"));
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

        form.addFormItem(typeService,"Tipo Servicio");
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
        FormLayout.FormItem supplierItem = form.addFormItem(layoutSupplier,"Proveedor/NIT");
        UIUtils.setColSpan(2,supplierItem);

        layoutSupplier.add(supplierName,nitSupplier,btnSearchSupplier);

        form.addFormItem(description,"Descripción");
        form.addFormItem(period,"Periodo");
        form.addFormItem(paymentDate,"Fecha de pago");
        form.addFormItem(amount,"Monto Bs.");
        form.addFormItem(account,"Cuenta");
        form.addFormItem(subAccount,"Subcuenta");
        form.addFormItem(typeDocumentReceived,"Tipo documento");
        form.addFormItem(numberDocumentReceived,"N° Factura/Recibo/CAABS");

        HorizontalLayout layoutContract = new HorizontalLayout();
        Button btnSearchContract = new Button();
        btnSearchContract.setWidth("10%");
        btnSearchContract.addThemeVariants(ButtonVariant.LUMO_SMALL,ButtonVariant.LUMO_PRIMARY);
        btnSearchContract.setIcon(VaadinIcon.FILE_SEARCH.create());
        btnSearchContract.addClickListener(event -> {
            if(supplierSelected==null){
                UIUtils.showNotificationType("Seleccione un proveedor para mostrar contratos disponibles","alert");
            }else {
                showSearchContract();
            }
        });
        FormLayout.FormItem contractItem = form.addFormItem(layoutContract,"N° contrato");
        UIUtils.setColSpan(1,contractItem);
        layoutContract.add(contract,btnSearchContract);

        form.addFormItem(finishDate,"Vigencia del contrato");
        form.addFormItem(tacitReductionClause,"");
        form.addFormItem(paymentFrecuency,"Frecuencia pago");



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


    private Grid searchSupplier(){
        List<Supplier> supplierList = supplierRestTemplate.getAll();
        ListDataProvider<Supplier> dataProvider = new ListDataProvider<>(supplierList);
        Grid<Supplier> grid = new Grid<>();
        grid.setWidthFull();
        grid.setDataProvider(dataProvider);

        grid.addColumn(Supplier::getName)
                .setSortable(true)
                .setAutoWidth(true)
                .setFlexGrow(1)
                .setKey("name")
                .setHeader("Proveedor");
        grid.addColumn(Supplier::getNit)
                .setSortable(true)
                .setAutoWidth(true)
                .setFlexGrow(1)
                .setKey("nit")
                .setHeader("NIT");
        grid.addColumn(Supplier::getLocation)
                .setSortable(true)
                .setAutoWidth(true)
                .setFlexGrow(1)
                .setKey("location")
                .setHeader("Ubicacion");
        grid.addColumn(Supplier::getPrimaryActivity)
                .setSortable(true)
                .setAutoWidth(true)
                .setFlexGrow(1)
                .setKey("primaryActivity")
                .setHeader("Actividad Principal");
        grid.addColumn(new ComponentRenderer<>(this::createButtonSelectSupplier))
                .setFlexGrow(0)
                .setAutoWidth(true);


        HeaderRow hr = grid.appendHeaderRow();

        supplierNameFilter = new TextField();
        supplierNameFilter.setWidthFull();
        supplierNameFilter.setValueChangeMode(ValueChangeMode.EAGER);
        supplierNameFilter.addValueChangeListener(e -> applyFilterSupplier(dataProvider));
        hr.getCell(grid.getColumnByKey("name")).setComponent(supplierNameFilter);

        nitSupplierFilter = new TextField();
        nitSupplierFilter.setWidthFull();
        nitSupplierFilter.setValueChangeMode(ValueChangeMode.EAGER);
        nitSupplierFilter.addValueChangeListener(e -> applyFilterSupplier(dataProvider));
        hr.getCell(grid.getColumnByKey("nit")).setComponent(nitSupplierFilter);

        locationSupplierFilter = new TextField();
        locationSupplierFilter.setWidthFull();
        locationSupplierFilter.setValueChangeMode(ValueChangeMode.EAGER);
        locationSupplierFilter.addValueChangeListener(e -> applyFilterSupplier(dataProvider));
        hr.getCell(grid.getColumnByKey("location")).setComponent(locationSupplierFilter);

        primaryActivitySupplierFilter = new TextField();
        primaryActivitySupplierFilter.setWidthFull();
        primaryActivitySupplierFilter.setValueChangeMode(ValueChangeMode.EAGER);
        primaryActivitySupplierFilter.addValueChangeListener(e -> applyFilterSupplier(dataProvider));
        hr.getCell(grid.getColumnByKey("primaryActivity")).setComponent(primaryActivitySupplierFilter);

        return grid;
    }

    private Component createButtonSelectSupplier(Supplier supplier){
        Button btn = new Button();
        btn.setIcon(VaadinIcon.CHEVRON_CIRCLE_UP.create());
        btn.addClickListener(event -> {
            supplierName.setValue(supplier.getName());
            nitSupplier.setValue(supplier.getNit());
            supplierSelected = supplier;

            detailsDrawer.hide();
        });

        return btn;
    }

    private void showSearchContract(){
        setViewDetails(createDetailDrawer());
        setViewDetailsPosition(Position.BOTTOM);
        detailsDrawerHeader.setTitle("Seleccionar Contrato");
        detailsDrawer.setContent(searchContract());
        detailsDrawer.show();
    }

    private Grid searchContract(){
        List<Contract> contractList =contractRestTemplate.getByIdSupplier(supplierSelected.getId().toString());

        ListDataProvider<Contract> dataprovider = new ListDataProvider<>(contractList);

        Grid<Contract> grid = new Grid<>();
        grid.setWidthFull();
        grid.setDataProvider(dataprovider);
        grid.addColumn(Contract::getNumberContract)
                .setSortable(true)
                .setAutoWidth(true)
                .setFlexGrow(1)
                .setKey("numberContract")
                .setHeader("Nro. Contrato");
        grid.addColumn(new LocalDateRenderer<>(Contract::getFinishDate,
                DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .setKey("finishDate")
                .setFlexGrow(1)
                .setSortable(true)
                .setHeader("Fecha finalización");
        grid.addColumn(Contract::getObjectContract)
                .setSortable(true)
                .setAutoWidth(true)
                .setFlexGrow(1)
                .setKey("objectContract")
                .setHeader("Objeto contrato");
        grid.addColumn(Contract::getCurrency)
                .setSortable(true)
                .setAutoWidth(true)
                .setFlexGrow(1)
                .setHeader("Moneda");
        grid.addColumn(new NumberRenderer<>(Contract::getAmount, " %(,.2f",
                        Locale.US, "0.00"))
                .setSortable(true)
                .setAutoWidth(true)
                .setFlexGrow(1)
                .setHeader("Monto");
        grid.addColumn(new ComponentRenderer<>(this::createButtonSelectContract))
                .setFlexGrow(0)
                .setAutoWidth(true);

        HeaderRow hr = grid.appendHeaderRow();

        numberContractFilter = new TextField();
        numberContractFilter.setWidthFull();
        numberContractFilter.setValueChangeMode(ValueChangeMode.EAGER);
        numberContractFilter.addValueChangeListener(e -> applyFilterContract(dataprovider));
        hr.getCell(grid.getColumnByKey("numberContract")).setComponent(numberContractFilter);

        objectContractFilter = new TextField();
        objectContractFilter.setWidthFull();
        objectContractFilter.setValueChangeMode(ValueChangeMode.EAGER);
        objectContractFilter.addValueChangeListener(e -> applyFilterContract(dataprovider));
        hr.getCell(grid.getColumnByKey("objectContract")).setComponent(objectContractFilter);

        return grid;
    }

    private Component createButtonSelectContract(Contract con){
        Button btn = new Button();
        btn.setIcon(VaadinIcon.CHEVRON_CIRCLE_UP.create());
        btn.addClickListener(event -> {
            contract.setValue(con.getNumberContract());
            finishDate.setValue(con.getFinishDate());
            tacitReductionClause.setValue(con.getTacitReductionClause());
            paymentFrecuency.setValue(con.getPaymentFrecuency());
            detailsDrawer.hide();
        });

        return btn;
    }


    private void applyFilterSupplier(ListDataProvider<Supplier> dataProvider){
        dataProvider.clearFilters();
        if(!supplierNameFilter.getValue().trim().equals("")){
            dataProvider.addFilter(supplier -> StringUtils.containsIgnoreCase(supplier.getName(),supplierNameFilter.getValue()));
        }
        if(nitSupplierFilter.getValue()!=null){
            dataProvider.addFilter(supplier -> Objects.equals(supplier.getNit(),nitSupplierFilter.getValue()));
        }
        if(!locationSupplierFilter.getValue().trim().equals("")){
            dataProvider.addFilter(supplier -> StringUtils.containsIgnoreCase(supplier.getLocation(),locationSupplierFilter.getValue()));
        }
        if(!primaryActivitySupplierFilter.getValue().trim().equals("")){
            dataProvider.addFilter(supplier -> StringUtils.containsIgnoreCase(supplier.getPrimaryActivity(),primaryActivitySupplierFilter.getValue()));
        }
    }

    private void applyFilterContract(ListDataProvider<Contract> dataProvider){
        if(!numberContractFilter.getValue().trim().equals("")){
            dataProvider.addFilter(contract -> StringUtils.containsIgnoreCase(contract.getNumberContract().toString(),numberContractFilter.getValue()));
        }
        if(!objectContractFilter.getValue().trim().equals("")){
            dataProvider.addFilter(contract -> StringUtils.containsIgnoreCase(contract.getObjectContract(),objectContractFilter.getValue()));
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
                .asRequired("Codigo Unidad de Sucursal es requerido")
                .bind(ExpenseDistribuite::getCodeFatherBusinessUnit,ExpenseDistribuite::setCodeFatherBusinessUnit);
        expenseDistribuiteBinder.forField(codeBusinessUnit)
                .asRequired("Codigo Unidad negocio es requerido")
                .bind(ExpenseDistribuite::getCodeBusinessUnit,ExpenseDistribuite::setCodeBusinessUnit);
        expenseDistribuiteBinder.forField(nameBusinessUnit)
                .asRequired("Nombre unidad negocio es requerido")
                .bind(ExpenseDistribuite::getNameBusinessUnit,ExpenseDistribuite::setNameBusinessUnit);
        expenseDistribuiteBinder.forField(amount)
                .asRequired("Monto es requerido")
                .withValidator(m -> m.doubleValue()>0.0,"Monto debe ser mayor a 0")
                .bind(ExpenseDistribuite::getAmount,ExpenseDistribuite::setAmount);

        expenseDistribuiteBinder.addStatusChangeListener(event -> {
            boolean isValid = !event.hasValidationErrors();
            boolean hasChanges = expenseDistribuiteBinder.hasChanges();
//            footer.saveState(hasChanges && isValid && GrantOptions.grantedOption("Parametros"));
            footerExpenseDistribuite.saveState(hasChanges && isValid && GrantOptions.grantedOptionWrite("Servicios Recurrentes")
                    && !recurrentServiceDto.getState().equals("FINALIZADO"));
        });


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

        FormLayout.FormItem amountItem = form.addFormItem(amount,"Monto");
        UIUtils.setColSpan(2,amountItem);
        expenseDistribuiteBinder.readBean(expenseDistribuite);
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
                footer.saveState(GrantOptions.grantedOptionWrite("Servicios Recurrentes") && !recurrentServiceDto.getState().equals("FINALIZADO"));

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
        Tooltips.getCurrent().setTooltip(btn,"Eliminar");
        btn.addClickListener(event -> {
            expenseDistribuiteList.remove(expenseDistribuite);
            expenseDistribuiteGrid.getDataProvider().refreshAll();
            footer.saveState(GrantOptions.grantedOptionWrite("Servicios Recurrentes") && !recurrentServiceDto.getState().equals("FINALIZADO"));
        });
        return btn;
    }

    private Component createButtonEdit(ExpenseDistribuite expenseDistribuite){
        Button btn = new Button();
        btn.setIcon(VaadinIcon.EDIT.create());
        btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Tooltips.getCurrent().setTooltip(btn,"Editar");
        btn.addClickListener(event -> {
            setViewDetailsPosition(Position.RIGHT);
            setViewDetails(createDetailsDrawerExpenseDistribuite());
            showDetailsExpenseDistribuite(expenseDistribuite);
        });

        return btn;
    }

    private RecurrentService fillRecurrentService() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        RecurrentService recurrentService = new RecurrentService();
        if(recurrentServiceDto.getId()==null){
            recurrentService.setCreatedBy(VaadinSession.getCurrent().getAttribute("login").toString());
        }else{
            recurrentService.setCreatedBy(recurrentServiceDto.getCreatedBy());
        }
        recurrentService.setId(recurrentServiceDto.getId());
        recurrentService.setTypeService(recurrentServiceDto.getTypeService());
        recurrentService.setIdSupplier(recurrentServiceDto.getIdSupplier());
        recurrentService.setDescription(recurrentServiceDto.getDescription());
        recurrentService.setPeriod(recurrentServiceDto.getPeriod());
        recurrentService.setPaymentDate(recurrentServiceDto.getPaymentDate());
        recurrentService.setAmount(recurrentServiceDto.getAmount());
        recurrentService.setAccount(recurrentServiceDto.getAccount());
        recurrentService.setSubAccount(recurrentServiceDto.getSubAccount());
        String json = mapper.writeValueAsString(expenseDistribuiteList);
        recurrentService.setExpenseDistribuite(json);
        recurrentService.setTypeDocumentReceived(recurrentServiceDto.getTypeDocumentReceived());
        recurrentService.setNumberDocumentReceived(recurrentServiceDto.getNumberDocumentReceived());
        recurrentService.setNumberContract(recurrentServiceDto.getNumberContract());
        String jsonInvoiceAuthorizer = mapper.writeValueAsString(selectedInvoiceAuthorizerList);
        recurrentService.setInvoiceAuthorizer(jsonInvoiceAuthorizer);
        recurrentService.setState(recurrentServiceDto.getState());
        recurrentService.setAccountingPerson(recurrentServiceDto.getAccountingPerson());
        recurrentService.setDateDeliveryAccounting(recurrentServiceDto.getDateDeliveryAccounting());
        return  recurrentService;
    }

    private boolean validateAmountExpenseDistribuite(){
        Double result = expenseDistribuiteList.stream()
                .mapToDouble(e -> e.getAmount()).sum();
       return (result.doubleValue() == recurrentServiceDto.getAmount().doubleValue());
    }

    //Invoice Authorizer
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
                footerInvoiceAuthorizer.saveState(GrantOptions.grantedOptionWrite("Servicios Recurrentes"));
                footer.saveState(GrantOptions.grantedOptionWrite("Servicios Recurrentes") && !recurrentServiceDto.getState().equals("FINALIZADO"));
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
        List<InvoiceAuthorizer> invoiceAuthorizerList = invoiceAuthorizerTemplate
                .getByCodeBranchOffice(Integer.valueOf(concept.getCode()));

        TextField codePosition = new TextField();
        codePosition.setWidthFull();
        codePosition.setRequired(true);
        codePosition.setReadOnly(true);

        TextField nameBranchOffice = new TextField();
        nameBranchOffice.setWidthFull();
        nameBranchOffice.setRequired(true);
        nameBranchOffice.setReadOnly(true);

        TextField priorityLevel = new TextField();
        priorityLevel.setRequired(true);
        priorityLevel.setReadOnly(true);
        priorityLevel.setWidthFull();

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
                .asRequired("Codigo Cargo es requerido")
                .bind(SelectedInvoiceAuthorizer::getCodePosition,SelectedInvoiceAuthorizer::setCodePosition);
        selectedInvoiceAuthorizerBinder.forField(nameBranchOffice)
                .asRequired("Unidad Negocio es requerida")
                .bind(SelectedInvoiceAuthorizer::getNameBranchOffice,SelectedInvoiceAuthorizer::setNameBranchOffice);
        selectedInvoiceAuthorizerBinder.forField(priorityLevel)
                .asRequired("Nivel Autorización es requerido")
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
        Tooltips.getCurrent().setTooltip(btn,"Eliminar");
        btn.addClickListener(event -> {
            selectedInvoiceAuthorizerList.remove(selectedInvoiceAuthorizer);
            selectedInvoiceAuthorizerGrid.getDataProvider().refreshAll();
        });

        return btn;
    }

    //  DELIVER ACCOUNTING

    private DetailsDrawer createDeliverAccounting(){

        dateDeliveryAccounting = new DatePicker("Fecha de entrega a contabilidad");
        dateDeliveryAccounting.setWidth("30%");
        dateDeliveryAccounting.setRequired(true);
        dateDeliveryAccounting.setClearButtonVisible(true);
        dateDeliveryAccounting.setRequiredIndicatorVisible(true);
        dateDeliveryAccounting.setLocale(new Locale("es","BO"));

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
                .bind(RecurrentServiceDto::getDateDeliveryAccounting, RecurrentServiceDto::setDateDeliveryAccounting);
        binder.forField(accountingPerson)
//                    .asRequired("Responsable de contabilidad es requerido")
                .bind(RecurrentServiceDto::getAccountingPerson, RecurrentServiceDto::setAccountingPerson);

        binder.addStatusChangeListener(event -> {
            boolean isValid = !event.hasValidationErrors();
            boolean hasChanges = binder.hasChanges();
            footer.saveState(isValid && hasChanges  && GrantOptions.grantedOptionWrite("Servicios Recurrentes")
                    && !recurrentServiceDto.getState().equals("FINALIZADO"));
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
