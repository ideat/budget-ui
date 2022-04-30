package com.mindware.ui.views.basicServices;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindware.backend.entity.basicServices.BasicServices;
import com.mindware.backend.entity.basicServices.BasicServicesDto;
import com.mindware.backend.entity.commonJson.ExpenseDistribuite;
import com.mindware.backend.entity.config.BasicServiceProvider;
import com.mindware.backend.entity.corebank.Concept;
import com.mindware.backend.rest.basicServiceProvider.BasicServiceProviderRestTemplate;
import com.mindware.backend.rest.basicServices.BasicServicesDtoRestTemplate;
import com.mindware.backend.rest.basicServices.BasicServicesRestTemplate;
import com.mindware.backend.rest.corebank.ConceptRestTemplate;
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
import com.mindware.ui.views.recurrentService.RecurrentServiceView;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.icon.Icon;
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
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.*;
import dev.mett.vaadin.tooltip.Tooltips;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@Route(value = "basicservices-register", layout = MainLayout.class)
@ParentLayout(MainLayout.class)
@PageTitle("Registro Pago Servicios Basicos")
public class BasicServicesRegisterView extends SplitViewFrame implements HasUrlParameter<String>, RouterLayout {

    @Autowired
    private BasicServicesRestTemplate basicServicesRestTemplate;

    @Autowired
    private BasicServicesDtoRestTemplate basicServicesDtoRestTemplate;

    @Autowired
    private BasicServiceProviderRestTemplate basicServiceProviderRestTemplate;

    @Autowired
    private UtilValues utilValues;

    @Autowired
    private ConceptRestTemplate conceptRestTemplate;

    private BasicServicesDto basicServicesDto;
    private BasicServiceProvider basicServiceProviderSelected;
    private List<ExpenseDistribuite> expenseDistribuiteList;

    private ListDataProvider<ExpenseDistribuite> expenseDistribuiteDataProvider;
    private Binder<BasicServicesDto> binder;
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

    private TextField nameBasicServiceProvider;
    private TextField description;
    private ComboBox<String> account;
    private ComboBox<String> subAccount;

    private Grid<ExpenseDistribuite> expenseDistribuiteGrid;

    private TextField basicServiceProviderFilter;
    private TextField typeServiceProviderFilter;
    private TextField descriptionServiceProviderFilter;

    private List<Concept> conceptList;

    @Override
    public void setParameter(BeforeEvent beforeEvent, @OptionalParameter String s) {
        mapper = new ObjectMapper();
        Location location = beforeEvent.getLocation();
        QueryParameters queryParameters = location.getQueryParameters();
        param = queryParameters.getParameters();

        if(!param.get("id").get(0).equals("NUEVO")){
            basicServicesDto = basicServicesDtoRestTemplate.getByIdBasicServices(param.get("id").get(0));
            title= "Proveedor: ".concat(basicServicesDto.getNameBasicServiceProvider());
            basicServiceProviderSelected = basicServiceProviderRestTemplate.getById(basicServicesDto.getIdBasicServicesProvider().toString());
        }else{
            basicServicesDto = new BasicServicesDto();
            basicServicesDto.setExpenseDistribuite("[]");
            title = "Registro Nuevo";
        }

        try {
            expenseDistribuiteList = mapper.readValue(basicServicesDto.getExpenseDistribuite(), new TypeReference<List<ExpenseDistribuite>>() {});
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
        setViewContent(createBasicServicesDtoForm(basicServicesDto));
        binder.readBean(basicServicesDto);

    }

    private AppBar initBar(){
        AppBar appBar = MainLayout.get().getAppBar();
        appBar.setNaviMode(AppBar.NaviMode.CONTEXTUAL);
        appBar.setTitle(title);
        appBar.getContextIcon().addClickListener(e -> UI.getCurrent().navigate("basicservices"));

        return appBar;
    }

    private DetailsDrawer createBasicServicesDtoForm(BasicServicesDto baseServicesDto){

        ComboBox<String> typeService = new ComboBox<>();
        typeService.setWidthFull();
        typeService.setItems(utilValues.getValueParameterByCategory("TIPO SERVICIO BASICO"));

        nameBasicServiceProvider = new TextField();
        nameBasicServiceProvider.setWidthFull();
        nameBasicServiceProvider.setReadOnly(true);
        nameBasicServiceProvider.setRequired(true);

        description = new TextField();
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

        binder = new BeanValidationBinder<>(BasicServicesDto.class);
        binder.forField(typeService)
                .asRequired("Tipo de servicio es requerido")
                .bind(BasicServicesDto::getTypeBasicService,BasicServicesDto::setTypeBasicService);
        binder.forField(nameBasicServiceProvider)
                .asRequired("Nombre Proveedor es requerido")
                .bind(BasicServicesDto::getNameBasicServiceProvider,BasicServicesDto::setNameBasicServiceProvider);
        binder.forField(description)
                .asRequired("Descripcion es requerida")
                .bind(BasicServicesDto::getDescription,BasicServicesDto::setDescription);
        binder.forField(period)
                .asRequired("Periodo es requerido")
                .bind(BasicServicesDto::getPeriod,BasicServicesDto::setPeriod);
        binder.forField(paymentDate)
                .asRequired("Fecha de pago es requerida")
                .bind(BasicServicesDto::getPaymentDate,BasicServicesDto::setPaymentDate);
        binder.forField(amount)
                .asRequired("Monto es requerido")
                .withValidator(m -> m.doubleValue()>0.0,"Monto debe ser mayor a 0")
                .bind(BasicServicesDto::getAmount,BasicServicesDto::setAmount);
        binder.forField(account)
                .asRequired("Cuenta es requerida")
                .bind(BasicServicesDto::getAccount,BasicServicesDto::setAccount);
        binder.forField(subAccount)
                .asRequired("Subcuenta es requerida")
                .bind(BasicServicesDto::getSubAccount,BasicServicesDto::setSubAccount);
        binder.forField(typeDocumentReceived)
                .asRequired("Tipo documento es requerido")
                .bind(BasicServicesDto::getTypeDocumentReceived, BasicServicesDto::setTypeDocumentReceived);
        binder.forField(numberDocumentReceived)
                .asRequired("Numero de Factura/Recibo es requerido")
                .bind(BasicServicesDto::getNumberDocumentReceived, BasicServicesDto::setNumberDocumentReceived);


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
        HorizontalLayout layoutProvider = new HorizontalLayout();
        Button btnSearchProvider = new Button();
        btnSearchProvider.setWidth("10%");
        btnSearchProvider.addThemeVariants(ButtonVariant.LUMO_SMALL,ButtonVariant.LUMO_PRIMARY);
        btnSearchProvider.setIcon(VaadinIcon.SEARCH_PLUS.create());
        btnSearchProvider.addClickListener(event -> {
            setViewDetails(createDetailDrawer());
            setViewDetailsPosition(Position.BOTTOM);
            showSearchProvider();
        });
        FormLayout.FormItem supplierItem = form.addFormItem(layoutProvider,"Proveedor");
        UIUtils.setColSpan(2,supplierItem);
        layoutProvider.add(nameBasicServiceProvider,btnSearchProvider);

        form.addFormItem(description,"Descripcion");
        form.addFormItem(period,"Periodo");
        form.addFormItem(paymentDate,"Fecha de pago");
        form.addFormItem(amount,"Monto (Bs)");
        form.addFormItem(account,"Cuenta");
        form.addFormItem(subAccount,"Subcuenta");
        form.addFormItem(typeDocumentReceived,"Tipo Documento");
        form.addFormItem(numberDocumentReceived,"Nro Factura/Recibo");

        footer = new DetailsDrawerFooter();
        footer.addSaveListener(event -> {
            try {
                if (binder.writeBeanIfValid(basicServicesDto)) {
                    basicServicesDto.setIdBasicServicesProvider(basicServiceProviderSelected.getId());

                    try {
                        basicServicesRestTemplate.add(fillBasicServices());
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }

                    UI.getCurrent().navigate(BasicServicesView.class);
                    UIUtils.showNotificationType("Datos registratos", "success");

                } else {

                }
            }catch (Exception e){
                UIUtils.showNotificationType(e.getMessage(), "error");
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

    private void showSearchProvider(){
        detailsDrawer.setPosition(DetailsDrawer.Position.BOTTOM);
        detailsDrawerHeader.setTitle("Seleccionar Proveedor");
        detailsDrawer.setContent(searchProvider());
        detailsDrawer.show();
    }

    private Grid searchProvider(){
        List<BasicServiceProvider> basicServiceProviderList = basicServiceProviderRestTemplate.getAll();
        ListDataProvider<BasicServiceProvider> dataProvider = new ListDataProvider<>(basicServiceProviderList);
        Grid<BasicServiceProvider> grid = new Grid<>();
        grid.setWidthFull();
        grid.setDataProvider(dataProvider);

        grid.addColumn(BasicServiceProvider::getProvider)
                .setSortable(true)
                .setAutoWidth(true)
                .setFlexGrow(1)
                .setKey("provider")
                .setHeader("Proveedor");
        grid.addColumn(BasicServiceProvider::getTypeService)
                .setSortable(true)
                .setAutoWidth(true)
                .setFlexGrow(1)
                .setKey("typeService")
                .setHeader("Tipo Servicio");
        grid.addColumn(BasicServiceProvider::getDescription)
                .setSortable(true)
                .setAutoWidth(true)
                .setFlexGrow(1)
                .setKey("description")
                .setHeader("Descripcion Servicio");
        grid.addColumn(new ComponentRenderer<>(this::createActive))
                .setSortable(true)
                .setAutoWidth(true)
                .setFlexGrow(1)
                .setHeader("Activo");
        grid.addColumn(new ComponentRenderer<>(this::createButtonSelectProvider))
                .setAutoWidth(true)
                .setFlexGrow(0);

        HeaderRow hr = grid.appendHeaderRow();

        basicServiceProviderFilter = new TextField();
        basicServiceProviderFilter.setWidthFull();
        basicServiceProviderFilter.setValueChangeMode(ValueChangeMode.EAGER);
        basicServiceProviderFilter.addValueChangeListener(e -> applyFilterBasicServiceProvider(dataProvider));
        hr.getCell(grid.getColumnByKey("provider")).setComponent(basicServiceProviderFilter);

        typeServiceProviderFilter = new TextField();
        typeServiceProviderFilter.setWidthFull();
        typeServiceProviderFilter.setValueChangeMode(ValueChangeMode.EAGER);
        typeServiceProviderFilter.addValueChangeListener(e -> applyFilterBasicServiceProvider(dataProvider));
        hr.getCell(grid.getColumnByKey("typeService")).setComponent(typeServiceProviderFilter);

        descriptionServiceProviderFilter = new TextField();
        descriptionServiceProviderFilter.setWidthFull();
        descriptionServiceProviderFilter.setValueChangeMode(ValueChangeMode.EAGER);
        descriptionServiceProviderFilter.addValueChangeListener(e -> applyFilterBasicServiceProvider(dataProvider));
        hr.getCell(grid.getColumnByKey("description")).setComponent(descriptionServiceProviderFilter);

         return grid;
    }

    private Component createActive(BasicServiceProvider basicServiceProvider){
        Icon icon;
        if(basicServiceProvider.getState().equals("ACTIVO")){
            icon = UIUtils.createPrimaryIcon(VaadinIcon.CHECK);
        }else{
            icon = UIUtils.createDisabledIcon(VaadinIcon.CLOSE);
        }
        return icon;
    }

    private Component createButtonSelectProvider(BasicServiceProvider basicServiceProvider){
        Button btn = new Button();
        btn.setIcon(VaadinIcon.CHEVRON_CIRCLE_UP.create());
        btn.addClickListener(event -> {
            nameBasicServiceProvider.setValue(basicServiceProvider.getProvider());
            basicServiceProviderSelected = basicServiceProvider;
            detailsDrawer.hide();
        });
        return btn;
    }

    private void applyFilterBasicServiceProvider(ListDataProvider<BasicServiceProvider> dataProvider){
        dataProvider.clearFilters();
        if(!basicServiceProviderFilter.getValue().trim().equals("")){
            dataProvider.addFilter(basicServiceProvider -> StringUtils.containsIgnoreCase(basicServiceProvider.getProvider()
                    ,basicServiceProviderFilter.getValue()));
        }
        if(!typeServiceProviderFilter.getValue().trim().equals("")){
            dataProvider.addFilter(basicServiceProvider -> StringUtils.containsIgnoreCase(basicServiceProvider.getTypeService()
                    ,typeServiceProviderFilter.getValue()));
        }
        if(!descriptionServiceProviderFilter.getValue().trim().equals("")){
            dataProvider.addFilter(basicServiceProvider -> StringUtils.containsIgnoreCase(basicServiceProvider.getDescription()
                    ,descriptionServiceProviderFilter.getValue()));
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

    private BasicServices fillBasicServices() throws JsonProcessingException {
        BasicServices basicServices = new BasicServices();
        basicServices.setId(basicServicesDto.getId());
        basicServices.setTypeBasicService(basicServicesDto.getTypeBasicService());
        basicServices.setIdBasicServicesProvider(basicServicesDto.getIdBasicServicesProvider());
        basicServices.setDescription(basicServicesDto.getDescription());
        basicServices.setPaymentDate(basicServicesDto.getPaymentDate());
        basicServices.setPeriod(basicServicesDto.getPeriod());
        basicServices.setAmount(basicServicesDto.getAmount());
        basicServices.setAccount(basicServicesDto.getAccount());
        basicServices.setSubAccount(basicServicesDto.getSubAccount());
        basicServices.setTypeDocumentReceived(basicServicesDto.getTypeDocumentReceived());
        basicServices.setNumberDocumentReceived(basicServicesDto.getNumberDocumentReceived());
        String json = mapper.writeValueAsString(expenseDistribuiteList);
        basicServices.setExpenseDistribuite(json);
        return basicServices;
    }
}
