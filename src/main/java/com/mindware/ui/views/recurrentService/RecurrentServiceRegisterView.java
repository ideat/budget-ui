package com.mindware.ui.views.recurrentService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindware.backend.entity.contract.Contract;
import com.mindware.backend.entity.corebank.Concept;
import com.mindware.backend.entity.commonJson.ExpenseDistribuite;
import com.mindware.backend.entity.recurrentService.RecurrentService;
import com.mindware.backend.entity.recurrentService.RecurrentServiceDto;
import com.mindware.backend.entity.supplier.Supplier;
import com.mindware.backend.rest.contract.ContractRestTemplate;
import com.mindware.backend.rest.corebank.ConceptRestTemplate;
import com.mindware.backend.rest.recurrentService.RecurrentServiceDtoRestTemplate;
import com.mindware.backend.rest.recurrentService.RecurrentServiceRestTemplate;
import com.mindware.backend.rest.supplier.SupplierRestTemplate;
import com.mindware.backend.util.UtilValues;
import com.mindware.ui.MainLayout;
import com.mindware.ui.components.detailsdrawer.DetailsDrawer;
import com.mindware.ui.components.detailsdrawer.DetailsDrawerFooter;
import com.mindware.ui.components.detailsdrawer.DetailsDrawerHeader;
import com.mindware.ui.components.navigation.bar.AppBar;
import com.mindware.ui.layout.size.Left;
import com.mindware.ui.layout.size.Right;
import com.mindware.ui.layout.size.Top;
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
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
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
import dev.mett.vaadin.tooltip.Tooltips;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.format.DateTimeFormatter;
import java.util.*;

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

    private Grid<ExpenseDistribuite> expenseDistribuiteGrid;

    private TextField supplierNameFilter;
    private TextField nitSupplierFilter;
    private TextField locationSupplierFilter;
    private TextField primaryActivitySupplierFilter;

    private TextField numberContractFilter;
    private TextField objectContractFilter;

    private DatePicker finishDate;
    private  Checkbox tacitReductionClause;


    private List<Concept> conceptList;

    private Map<String, List<String>> param;

    @Override
    public void setParameter(BeforeEvent beforeEvent, @OptionalParameter String s) {
        mapper = new ObjectMapper();
        Location location = beforeEvent.getLocation();
        QueryParameters queryParameters = location.getQueryParameters();
        param = queryParameters.getParameters();

        if(!param.get("id").get(0).equals("NUEVO")){
            recurrentServiceDto = recurrentServiceDtoRestTemplate.getById(param.get("id").get(0));
            title = "Proveedor: ".concat(recurrentServiceDto.getSupplierName());
            supplierSelected = supplierRestTemplate.getById(recurrentServiceDto.getIdSupplier().toString());


        }else{
            recurrentServiceDto = new RecurrentServiceDto();
            recurrentServiceDto.setExpenseDistribuite("[]");
            title = "Registro Nuevo ";
        }

        try {
            expenseDistribuiteList = mapper.readValue(recurrentServiceDto.getExpenseDistribuite(), new TypeReference<List<ExpenseDistribuite>>() {});
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        expenseDistribuiteDataProvider = new ListDataProvider<>(expenseDistribuiteList);

        conceptList = new ArrayList<>(conceptRestTemplate.getAgencia());
        conceptList.addAll(conceptRestTemplate.getSucursal());
        conceptList.sort(Comparator.comparing(Concept::getCode));

        setViewDetails(createDetailDrawer());
        setViewDetailsPosition(Position.BOTTOM);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent){
        super.onAttach(attachEvent);
        initBar();
        setViewContent(createRecurrentServiceDtoForm(recurrentServiceDto));
        binder.readBean(recurrentServiceDto);
    }

    private AppBar initBar(){
        AppBar appBar = MainLayout.get().getAppBar();
        appBar.setNaviMode(AppBar.NaviMode.CONTEXTUAL);
        appBar.setTitle(title);
        appBar.getContextIcon().addClickListener(e -> UI.getCurrent().navigate("recurrent-service"));

        return appBar;
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

        DatePicker paymentDate = new DatePicker();
        paymentDate.setWidthFull();
        paymentDate.setRequired(true);
        paymentDate.setLocale(new Locale("es","BO"));


        NumberField amount = new NumberField();
        amount.setWidthFull();
        amount.setRequiredIndicatorVisible(true);
        amount.setMin(0.0);

        account = new ComboBox<>();
        account.setWidthFull();
        account.setItems(utilValues.getAccounts());
        account.setRequired(true);
        account.setRequiredIndicatorVisible(true);
        account.addValueChangeListener(e -> {
            subAccount.clear();
            subAccount.setItems(utilValues.getSubAccounts(e.getValue()));
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
                .asRequired("Numero Factura/Recibo es requerido")
                .bind(RecurrentServiceDto::getNumberDocumentReceived,RecurrentServiceDto::setNumberDocumentReceived);
        binder.forField(contract)
                .asRequired("Numero contrato es requerido")
                .bind(RecurrentServiceDto::getNumberContract,RecurrentServiceDto::setNumberContract);
        binder.forField(finishDate)
                .asRequired("Fecha vigencia contrato es requerido")
                .bind(RecurrentServiceDto::getFinishDate,RecurrentServiceDto::setFinishDate);
        binder.forField(tacitReductionClause)
                .bind(RecurrentServiceDto::getTacitReductionClause,RecurrentServiceDto::setTacitReductionClause);
        binder.forField(paymentFrecuency)
                .bind(RecurrentServiceDto::getPaymentFrecuency,RecurrentServiceDto::setPaymentFrecuency);

        binder.addStatusChangeListener(event -> {
           boolean isValid = !event.hasValidationErrors();
           boolean hasChanges = binder.hasChanges();
           footer.saveState(isValid && hasChanges);
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
        form.addFormItem(numberDocumentReceived,"Número Factura/Recibo");

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
        FormLayout.FormItem contractItem = form.addFormItem(layoutContract,"Número contrato");
        UIUtils.setColSpan(1,contractItem);
        layoutContract.add(contract,btnSearchContract);

        form.addFormItem(finishDate,"Vigencia del contrato");
        form.addFormItem(tacitReductionClause,"");
        form.addFormItem(paymentFrecuency,"Frecuencia pago");

        footer = new DetailsDrawerFooter();
        footer.addSaveListener(event ->{
            if(binder.writeBeanIfValid(recurrentServiceDto)){
                if(validateAmountExpenseDistribuite()) {
                    recurrentServiceDto.setIdSupplier(supplierSelected.getId());
                    try {
                        recurrentServiceRestTemplate.add(fillRecurrentService());
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    UI.getCurrent().navigate(RecurrentServiceView.class);
                    UIUtils.showNotificationType("Datos registratos", "success");
                }else{
                    UIUtils.showNotificationType("Monto distribuido no cuadra con el monto del contrato","alert");
                }
            }
        });
        footer.addCancelListener(event -> UI.getCurrent().navigate(RecurrentServiceView.class));

        DetailsDrawer detailsDrawer = new DetailsDrawer(DetailsDrawer.Position.BOTTOM);
        detailsDrawer.setHeight("100%");
        detailsDrawer.setPadding(Left.S, Right.S, Top.S);
        detailsDrawer.setContent(form, gridExpenseDistribuite());
        detailsDrawer.setFooter(footer);
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
        if(!nitSupplierFilter.getValue().trim().equals("")){
            dataProvider.addFilter(supplier -> StringUtils.containsIgnoreCase(supplier.getNit(),nitSupplierFilter.getValue()));
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
        unitBusiness.addValueChangeListener(event -> {
            if(event.getValue() != null) {
                Optional<ExpenseDistribuite> opt = expenseDistribuiteList.stream()
                        .filter(exp -> exp.getCodeBusinessUnit().equals(Integer.parseInt(event.getValue().getCode())))
                        .findFirst();
                if (!opt.isPresent()) {
                    codeBusinessUnit.setValue(Integer.valueOf(event.getValue().getCode()));
                    nameBusinessUnit.setValue(event.getValue().getDescription());
                } else {
                    UIUtils.showNotificationType("Unidad de Negocio ya fue agregada", "alert");
                    unitBusiness.clear();
                    codeBusinessUnit.clear();
                    nameBusinessUnit.clear();
                }
            }
        });

        expenseDistribuiteBinder = new BeanValidationBinder<>(ExpenseDistribuite.class);
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
            footerExpenseDistribuite.saveState(hasChanges && isValid);
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
                footer.saveState(true); //TODO HABILITAR SI TIENE EL ROL

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
            footer.saveState(true); //TODO HABILITAR SI TIENE EL ROL
        });

        return btn;

    }



    private RecurrentService fillRecurrentService() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        RecurrentService recurrentService = new RecurrentService();
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
        return  recurrentService;
    }

    private boolean validateAmountExpenseDistribuite(){
        Double result = expenseDistribuiteList.stream()
                .mapToDouble(e -> e.getAmount()).sum();
       return (result.doubleValue() == recurrentServiceDto.getAmount().doubleValue());
    }
}
