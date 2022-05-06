package com.mindware.ui.views.acquisition;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.mindware.backend.entity.adquisition.Acquisition;
import com.mindware.backend.entity.adquisition.Item;
import com.mindware.backend.entity.commonJson.ExpenseDistribuite;
import com.mindware.backend.entity.corebank.Concept;
import com.mindware.backend.entity.user.UserLdapDto;
import com.mindware.backend.rest.acquisition.AcquisitionRestTemplate;
import com.mindware.backend.rest.corebank.ConceptRestTemplate;
import com.mindware.backend.rest.dataLdap.DataLdapRestTemplate;
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
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.*;
import dev.mett.vaadin.tooltip.Tooltips;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.*;

@Route(value = "acquisition-register", layout = MainLayout.class)
@ParentLayout(MainLayout.class)
@PageTitle("Registro Adquisiciones")
public class AcquisitionRegisterView extends SplitViewFrame implements RouterLayout,HasUrlParameter<String> {

    @Autowired
    private ConceptRestTemplate conceptRestTemplate;

    @Autowired
    private UtilValues utilValues;

    @Autowired
    private AcquisitionRestTemplate acquisitionRestTemplate;

    @Autowired
    private DataLdapRestTemplate dataLdapRestTemplate;

    private Map<String, List<String>> params;
    private ObjectMapper mapper;

    private IntegerField numberRequest;
    private TextField businessUnit;
    private IntegerField codeBusinessUnit;
    private TextField applicant;
    private TextField areaApplicant;
    private ComboBox<String> typeRequest;
    private DatePicker receptionDate;

    private Acquisition current;

    private List<Item> items;
    private Binder<Acquisition> binder;
    private Binder<Acquisition> binderQuoationRequest;

    private DetailsDrawerFooter footer;
    private DetailsDrawer detailsDrawer;
    private DetailsDrawerHeader detailsDrawerHeader;

    private DetailsDrawerFooter footerExpenseDistribuite;
    private DetailsDrawer detailsDrawerExpenseDistribuite;
    private DetailsDrawerHeader detailsDrawerHeaderExpenseDistribuite;
    private Binder<ExpenseDistribuite> expenseDistribuiteBinder;
    private ListDataProvider<ExpenseDistribuite> expenseDistribuiteDataProvider;
    private ExpenseDistribuite currentExpenseDistribuite;
    private List<ExpenseDistribuite> expenseDistribuiteList;
    private Grid<ExpenseDistribuite> expenseDistribuiteGrid;

    private DetailsDrawerFooter footerItem;
    private DetailsDrawer detailsDrawerItem;
    private DetailsDrawerHeader detailsDrawerHeaderItem;
    private Binder<Item> itemBinder;
    private ListDataProvider<Item> itemDataprovider;
    private Item currentItem;
    private List<Item> itemList;
    private Grid<Item> itemGrid;


    private List<Concept> conceptList;
    private String title;

    private FlexBoxLayout contentPurchaseRequest;
    private FlexBoxLayout contentInformationQuote; //informacion sobre cotizacion

//    Filter Concept Agency
    private TextField codeFilter;
    private TextField code2Filter;
    private TextField descriptionFilter;

//    Filter User Agency
    private TextField cnFilter;
    private TextField titleFilter;

    @SneakyThrows
    @Override
    public void setParameter(BeforeEvent beforeEvent, @OptionalParameter String s) {
        mapper = new ObjectMapper();
        Location location = beforeEvent.getLocation();
        QueryParameters queryParameters = location.getQueryParameters();
        params = queryParameters.getParameters();

        conceptList = conceptRestTemplate.getAgencia();

        footer = new DetailsDrawerFooter();
        if(!params.get("id").get(0).equals("NUEVO")){
            current = acquisitionRestTemplate.getById(params.get("id").get(0));
            title = "Adquición";
//            codeBusinessUnit.setValue(current.getCodeBusinessUnit());

            Concept concept = conceptList.stream()
                    .filter(c -> c.getCode2().equals(String.valueOf(current.getCodeBusinessUnit())))
                    .findFirst().get();
//            businessUnit.setValue(concept.getDescription());

            itemList = mapper.readValue(current.getItems(), new TypeReference<List<Item>>() {});
            itemDataprovider = new ListDataProvider<>(itemList);

            contentPurchaseRequest = (FlexBoxLayout) createContent(createPurchaseRequest(current));
            contentInformationQuote = (FlexBoxLayout) createContent(createInformationQuote(current));



            setViewContent(contentPurchaseRequest,contentInformationQuote);

        }else{
            current = new Acquisition();
            current.setAuthorizersLevel1("[]");
            current.setAuthorizersLevel2("[]");
            current.setExpenseDistribuite("[]");
            current.setItems("[]");
            current.setAdjudicationInformation("[]");
            current.setReceptionInformation("[]");
            current.setInvoiceInformation("[]");
            title = "Adquición";

            itemList = mapper.readValue(current.getItems(), new TypeReference<List<Item>>() {});
            itemDataprovider = new ListDataProvider<>(itemList);

            contentPurchaseRequest = (FlexBoxLayout) createContent(createPurchaseRequest(current));
            contentInformationQuote = (FlexBoxLayout) createContent(createInformationQuote(current));

            setViewContent(contentPurchaseRequest,contentInformationQuote);
        }

        conceptList = new ArrayList<>(conceptRestTemplate.getAgencia());
        conceptList.addAll(conceptRestTemplate.getSucursal());
        conceptList.sort(Comparator.comparing(Concept::getCode));

        expenseDistribuiteList = mapper.readValue(current.getExpenseDistribuite(),new TypeReference<List<ExpenseDistribuite>>(){});
        expenseDistribuiteDataProvider = new ListDataProvider<>(expenseDistribuiteList);

        setViewDetails(createDetailDrawer());
        setViewDetailsPosition(Position.BOTTOM);

        footer.addSaveListener(event -> {
            if(itemList.size()<=0){
                UIUtils.showNotificationType("Registre Items para su adquisicion","alert");
                return;
            }
            if(binder.writeBeanIfValid(current)){
                try {
                    String jsonItems = mapper.writeValueAsString(itemList);
                    current.setItems(jsonItems);
                    current = acquisitionRestTemplate.add(current);
                    numberRequest.setValue(current.getAcquisitionNumber());
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        });
        setViewFooter(footer);
        binder.readBean(current);
    }

    @Override
    public void onAttach(AttachEvent attachEvent){
        super.onAttach(attachEvent);
        AppBar appBar = initAppBar();
        if(params.get("id").get(0).equals("NUEVO")){
            appBar.setTitle("Nueva Adquisicion");
        }else{
            appBar.setTitle("Adquisicion Nro:"+ params.get("numberRequest").get(0));
        }
        UI.getCurrent().getPage().setTitle("");

    }

    private AppBar initAppBar(){
        MainLayout.get().getAppBar().reset();
        AppBar appBar = MainLayout.get().getAppBar();

        appBar.addTab("Solicitud de Compra");
        appBar.addTab("Información Cotización");
        appBar.addTab("Información CAABS");
        appBar.addTab("Información Adjudicación");
        appBar.addTab("Recepción del Bien o Servicio");
        appBar.addTab("Información de la Factura");
        appBar.addTab("Distribución del Gasto");
        appBar.addTab("Entrega a Contabilidad y a la AAAF");

        appBar.centerTabs();

        appBar.addTabSelectionListener(e -> {
            enabledSheets();
            if(e.getSource().getSelectedTab()!=null){
                Tab selectedTab = appBar.getSelectedTab();

                if(selectedTab.getLabel().equals("Solicitud de Compra")){
                    contentPurchaseRequest.setVisible(true);
                    contentInformationQuote.setVisible(false);
                }else if(selectedTab.getLabel().equals("Información Cotización")){
                    contentPurchaseRequest.setVisible(false);
                    contentInformationQuote.setVisible(true);
                }
            }
        });

        appBar.setNaviMode(AppBar.NaviMode.CONTEXTUAL);
        appBar.getContextIcon().addClickListener( e -> UI.getCurrent().navigate(AcquisitionView.class));
        return appBar;
    }

    private void enabledSheets(){
        if(current.getAcquisitionNumber()==null){
            contentInformationQuote.setEnabled(false);
        }else{
            contentInformationQuote.setEnabled(true);
        }
    }

    private DetailsDrawer createDetailDrawer(){
        detailsDrawer = new DetailsDrawer(DetailsDrawer.Position.BOTTOM);
        detailsDrawer.setWidthFull();
        // Header
        detailsDrawerHeader = new DetailsDrawerHeader("");
        detailsDrawerHeader.addCloseListener(buttonClickEvent -> detailsDrawer.hide());
        detailsDrawer.setHeader(detailsDrawerHeader);

        return detailsDrawer;
    }

    private Component createContent(DetailsDrawer component){
        FlexBoxLayout content = new FlexBoxLayout(component);
        content.setFlexDirection(FlexLayout.FlexDirection.ROW);
        content.setMargin(Vertical.AUTO, Vertical.RESPONSIVE_L);
        content.setSizeFull();

        return content;
    }

//    PURCHASE REQUEST
    private DetailsDrawer createPurchaseRequest(Acquisition acquisition){
        numberRequest = new IntegerField();
        numberRequest.setWidthFull();
        numberRequest.setReadOnly(true);

        codeBusinessUnit = new IntegerField();
        codeBusinessUnit.setWidth("30%");
        codeBusinessUnit.setReadOnly(true);

        businessUnit = new TextField();
        businessUnit.setWidth("60%");
        businessUnit.setReadOnly(true);
        businessUnit.setRequired(true);

        applicant = new TextField();
        applicant.setWidthFull();
        applicant.setReadOnly(true);
        applicant.setRequired(true);

        areaApplicant = new TextField();
        areaApplicant.setWidthFull();
        areaApplicant.setReadOnly(true);
        areaApplicant.setRequired(true);

        typeRequest = new ComboBox<>();
        typeRequest.setRequired(true);
        typeRequest.setWidthFull();
        typeRequest.setItems(utilValues.getValueParameterByCategory("TIPO ADQUISICION"));
        typeRequest.setAutoOpen(true);

        receptionDate = new DatePicker();
        receptionDate.setWidthFull();
        receptionDate.setRequired(true);
        receptionDate.setLocale(new Locale("es","BO"));

        binder = new BeanValidationBinder<>(Acquisition.class);
        binder.forField(numberRequest)
                .bind(Acquisition::getAcquisitionNumber,Acquisition::setAcquisitionNumber);
        binder.forField(codeBusinessUnit)
                .asRequired("Codigo Unidad de Negocio es requerido")
                .bind(Acquisition::getCodeBusinessUnit,Acquisition::setCodeBusinessUnit);
        binder.forField(businessUnit)
                .asRequired("Nombre Unidad de Negocio es requerido")
                        .bind(Acquisition::getNameBusinessUnit,Acquisition::setNameBusinessUnit);
        binder.forField(applicant)
                .asRequired("Solicitante es requerido")
                .bind(Acquisition::getApplicant,Acquisition::setApplicant);
        binder.forField(areaApplicant)
                .asRequired("Area de trabajo del solicitante es requerida")
                .bind(Acquisition::getAreaApplicant,Acquisition::setAreaApplicant);
        binder.forField(typeRequest)
                .asRequired("Tipo de solicitud es requerido")
                .bind(Acquisition::getTypeRequest,Acquisition::setTypeRequest);
        binder.forField(receptionDate)
                .asRequired("Fecha de Recepcion es requerida")
                .bind(Acquisition::getReceptionDate,Acquisition::setReceptionDate);

        binder.addStatusChangeListener(event -> {
            boolean isValid = !event.hasValidationErrors();
            boolean hasChanges = binder.hasChanges();
            footer.saveState(isValid && hasChanges);
        });

        FormLayout form = new FormLayout();
        form.setWidthFull();
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

        form.addFormItem(numberRequest, "Nro. Solicitud");
        HorizontalLayout layoutBusinessUnit = new HorizontalLayout();
        Button btnSearchBusinessUnit = new Button();
        btnSearchBusinessUnit.setWidth("10%");
        btnSearchBusinessUnit.addThemeVariants(ButtonVariant.LUMO_SMALL,ButtonVariant.LUMO_PRIMARY);
        btnSearchBusinessUnit.setIcon(VaadinIcon.SEARCH_PLUS.create());
        btnSearchBusinessUnit.addClickListener(event -> {
            setViewDetails(createDetailDrawer());
            setViewDetailsPosition(Position.BOTTOM);
            showSearchBusinessUnit();
        });

        layoutBusinessUnit.add(codeBusinessUnit,businessUnit,btnSearchBusinessUnit);
        FormLayout.FormItem businessUnitItem = form.addFormItem(layoutBusinessUnit,"Unidad Negocio: Agencias y Sucursales");
        UIUtils.setColSpan(2,businessUnitItem);

        HorizontalLayout layoutApplicant = new HorizontalLayout();
        Button btnSearchApplicant = new Button();
        btnSearchApplicant.setWidth("10%");
        btnSearchApplicant.addThemeVariants(ButtonVariant.LUMO_SMALL,ButtonVariant.LUMO_PRIMARY);
        btnSearchApplicant.setIcon(VaadinIcon.SEARCH_PLUS.create());
        btnSearchApplicant.addClickListener(event -> {
            if(!codeBusinessUnit.isEmpty()) {
                setViewDetails(createDetailDrawer());
                setViewDetailsPosition(Position.BOTTOM);
                showSearchApplicant();
            }else{
                UIUtils.showNotificationType("Seleccione una Unidad de Negocio", "alert");
            }
        });
        layoutApplicant.add(applicant,btnSearchApplicant);
        FormLayout.FormItem applicantItem = form.addFormItem(layoutApplicant,"Solicitante");
        UIUtils.setColSpan(1,applicantItem);

        form.addFormItem(areaApplicant,"Area Solicitante");
//        footer = new DetailsDrawerFooter();

        form.addFormItem(typeRequest,"Tipo adquisición");
        form.addFormItem(receptionDate,"Fecha de recepción");

        DetailsDrawer detailsDrawer = new DetailsDrawer(DetailsDrawer.Position.BOTTOM);
        detailsDrawer.setWidthFull();
        detailsDrawer.setHeight("90%");

        detailsDrawer.setPadding(Left.S, Right.S, Top.S);
        detailsDrawer.setContent(form, gridItems());
        detailsDrawer.show();


        return detailsDrawer;

    }

    private void showSearchBusinessUnit(){

        detailsDrawerHeader.setTitle("Seleccionar Unidad de Negocio");
        detailsDrawer.setContent(searchBusinessUnit());
        detailsDrawer.show();
    }

    private Grid searchBusinessUnit(){

        ListDataProvider<Concept> data = new ListDataProvider<>(conceptList);
        Grid<Concept> grid = new Grid<>();
        grid.setWidthFull();
        grid.setDataProvider(data);
        grid.addColumn(Concept::getCode)
                .setSortable(true)
                .setKey("code")
                .setAutoWidth(true)
                .setFlexGrow(1)
                .setHeader("Codigo Sucursal");
        grid.addColumn(Concept::getCode2)
                .setSortable(true)
                .setKey("code2")
                .setAutoWidth(true)
                .setFlexGrow(1)
                .setHeader("Codigo Agencia");
        grid.addColumn(Concept::getDescription)
                .setSortable(true)
                .setKey("description")
                .setAutoWidth(true)
                .setFlexGrow(1)
                .setHeader("Unidad de Negocio");
        grid.addColumn(new ComponentRenderer<>(this::createButtonSelectBusinessUnit))
                .setAutoWidth(true)
                .setFlexGrow(1);

        HeaderRow hr = grid.appendHeaderRow();

        codeFilter = new TextField();
        codeFilter.setValueChangeMode(ValueChangeMode.EAGER);
        codeFilter.setWidthFull();
        codeFilter.addValueChangeListener(e -> applyFilterBusinessUnit(data));
        hr.getCell(grid.getColumnByKey("code")).setComponent(codeFilter);

        code2Filter = new TextField();
        code2Filter.setValueChangeMode(ValueChangeMode.EAGER);
        code2Filter.setWidthFull();
        code2Filter.addValueChangeListener(e -> applyFilterBusinessUnit(data));
        hr.getCell(grid.getColumnByKey("code2")).setComponent(code2Filter);

        descriptionFilter = new TextField();
        descriptionFilter.setValueChangeMode(ValueChangeMode.EAGER);
        descriptionFilter.setWidthFull();
        descriptionFilter.addValueChangeListener(e -> applyFilterBusinessUnit(data));
        hr.getCell(grid.getColumnByKey("description")).setComponent(descriptionFilter);

        return grid;
    }

    private Component createButtonSelectBusinessUnit(Concept concept){
        Button btn = new Button();
        btn.setIcon(VaadinIcon.CHEVRON_CIRCLE_UP.create());
        btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Tooltips.getCurrent().setTooltip(btn,"Selecciona Unidad Negocio");
        btn.addClickListener(event -> {
            codeBusinessUnit.setValue(Integer.valueOf(concept.getCode2()));
            businessUnit.setValue(concept.getDescription());
            detailsDrawer.hide();
        });

        return btn;
    }

    private void applyFilterBusinessUnit(ListDataProvider<Concept> dataProvider){
        dataProvider.clearFilters();
        if(!codeFilter.getValue().trim().equals("")){
            dataProvider.addFilter(concept -> StringUtils.containsIgnoreCase(
                    concept.getCode(),codeFilter.getValue()));
        }
        if(!code2Filter.getValue().trim().equals("")){
            dataProvider.addFilter(concept -> StringUtils.containsIgnoreCase(
                    concept.getCode2(),code2Filter.getValue()));
        }
        if(!descriptionFilter.getValue().trim().equals("")){
            dataProvider.addFilter(concept -> StringUtils.containsIgnoreCase(
                    concept.getDescription(),descriptionFilter.getValue()));
        }
    }

    private void showSearchApplicant(){
        detailsDrawerHeader.setTitle("Seleccionar Funcionario Unidad de Negocio");
        detailsDrawer.setContent(searchApplicant());
        detailsDrawer.show();
    }

    private Grid searchApplicant(){
        List<UserLdapDto> userLdapDtoList = dataLdapRestTemplate.getByCodeBusinessUnit(codeBusinessUnit.getValue());
        ListDataProvider<UserLdapDto> data = new ListDataProvider<>(userLdapDtoList);
        Grid<UserLdapDto> grid = new Grid<>();
        grid.setWidthFull();
        grid.setDataProvider(data);
        grid.addColumn(UserLdapDto::getCn)
                .setSortable(true)
                .setKey("cn")
                .setAutoWidth(true)
                .setFlexGrow(1)
                .setHeader("Nombre Funcionario");
        grid.addColumn(UserLdapDto::getTitle)
                .setSortable(true)
                .setKey("title")
                .setAutoWidth(true)
                .setFlexGrow(1)
                .setHeader("Cargo");
        grid.addColumn(new ComponentRenderer<>(this::createButtonSelectApplicant))
                .setAutoWidth(true)
                .setFlexGrow(1);

        HeaderRow hr = grid.appendHeaderRow();

        cnFilter = new TextField();
        cnFilter.setValueChangeMode(ValueChangeMode.EAGER);
        cnFilter.setWidthFull();
        cnFilter.addValueChangeListener(e -> applicantFilterApplicant(data));
        hr.getCell(grid.getColumnByKey("cn")).setComponent(cnFilter);

        titleFilter = new TextField();
        titleFilter.setValueChangeMode(ValueChangeMode.EAGER);
        titleFilter.setWidthFull();
        titleFilter.addValueChangeListener(e -> applicantFilterApplicant(data));
        hr.getCell(grid.getColumnByKey("title")).setComponent(titleFilter);

        return grid;
    }

    private Component createButtonSelectApplicant(UserLdapDto userLdapDto){
        Button btn = new Button();
        btn.setIcon(VaadinIcon.CHEVRON_CIRCLE_UP.create());
        btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Tooltips.getCurrent().setTooltip(btn,"Selecciona Solicitante");
        btn.addClickListener(event -> {
            applicant.setValue(userLdapDto.getCn());
            areaApplicant.setValue(userLdapDto.getDepartament());
            detailsDrawer.hide();
        });

        return btn;
    }

    private void applicantFilterApplicant(ListDataProvider<UserLdapDto> dataProvider){
        dataProvider.clearFilters();
        if(!cnFilter.getValue().trim().equals("")){
            dataProvider.addFilter(userLdapDto -> StringUtils.containsIgnoreCase(
                    userLdapDto.getCn(),cnFilter.getValue()));
        }
        if(!titleFilter.getValue().trim().equals("")){
            dataProvider.addFilter(concept -> StringUtils.containsIgnoreCase(
                    concept.getTitle(),titleFilter.getValue()));
        }
    }

//    END PURCHASE REQUEST

//***    ITEMS
    private FormLayout layoutItem(Item item){

        IntegerField quantity = new IntegerField();
        quantity.setMin(0);
        quantity.setRequiredIndicatorVisible(true);
        quantity.setWidthFull();

        TextField description = new TextField();
        description.setWidthFull();
        description.setRequired(true);

        itemBinder = new BeanValidationBinder<>(Item.class);
        itemBinder.forField(quantity)
                .asRequired("Cantidad es querida")
                .withValidator(i -> i.intValue()>0,"Cantidad debe ser mayor a 0")
                .bind(Item::getQuantity,Item::setQuantity);
        itemBinder.forField(description)
                .asRequired("Descripción es requerida")
                .bind(Item::getDescription,Item::setDescription);
        itemBinder.addStatusChangeListener(event ->{
            boolean isValid = !event.hasValidationErrors();
            boolean hasChanges = itemBinder.hasChanges();
            footerItem.saveState(hasChanges && isValid);
        });

        FormLayout form = new FormLayout();
        form.addClassNames(LumoStyles.Padding.Bottom.L,
                LumoStyles.Padding.Horizontal.S, LumoStyles.Padding.Top.S);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1,
                        FormLayout.ResponsiveStep.LabelsPosition.TOP),
                new FormLayout.ResponsiveStep("21em", 2,
                        FormLayout.ResponsiveStep.LabelsPosition.TOP));

        FormLayout.FormItem quantityItem = form.addFormItem(quantity,"Cantidad");
        UIUtils.setColSpan(2,quantityItem);
        FormLayout.FormItem descriptionItem = form.addFormItem(description,"Descripción");
        UIUtils.setColSpan(2,descriptionItem);

        return form;
    }

    private void showDetailsItem(Item item){
        setViewDetails(createDetailsDrawerItem());
        setViewDetailsPosition(Position.RIGHT);
        currentItem = item;
        detailsDrawerHeaderItem.setTitle("Item: ".concat(item.getDescription()==null?"Nuevo Item":item.getDescription()));
        detailsDrawerItem.setContent(layoutItem(currentItem));
        detailsDrawerItem.show();
    }

    private DetailsDrawer createDetailsDrawerItem(){
        detailsDrawerItem = new DetailsDrawer(DetailsDrawer.Position.RIGHT);

        detailsDrawerHeaderItem = new DetailsDrawerHeader("");
        detailsDrawerHeaderItem.addCloseListener(event -> detailsDrawerItem.hide());
        detailsDrawerItem.setHeader(detailsDrawerHeaderItem);

        footerItem = new DetailsDrawerFooter();
        footerItem.addSaveListener(e -> {
            if(currentItem !=null && itemBinder.writeBeanIfValid(currentItem)){

                itemList.removeIf(ed -> ed.getId().equals(currentItem.getId()));
                currentItem.setId(UUID.randomUUID());
                itemList.add(currentItem);
                detailsDrawerItem.hide();
                itemGrid.getDataProvider().refreshAll();
                footer.saveState(true); //TODO HABILITAR SI TIENE EL ROL

            }
        });
        footerItem.addCancelListener(e -> detailsDrawerItem.hide());
        detailsDrawerItem.setFooter(footerItem);

        return detailsDrawerItem;
    }

    private VerticalLayout gridItems(){

        VerticalLayout layout = new VerticalLayout();
        layout.setWidthFull();
        Button btnAdd = new Button("Adicionar");
        btnAdd.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_CONTRAST,ButtonVariant.LUMO_SMALL);
        btnAdd.addClickListener(event -> {
            setViewDetailsPosition(Position.RIGHT);
            setViewDetails(createDetailsDrawerItem());
            showDetailsItem(new Item());
        });

        itemGrid = new Grid<>();
        itemGrid.setWidthFull();
//        itemGrid.setHeight("80%");
        itemGrid.setDataProvider(itemDataprovider);
        itemGrid.addColumn(Item::getQuantity)
                .setSortable(true)
                .setAutoWidth(true)
                .setResizable(true)
                .setFlexGrow(0)
                .setHeader("Cantidad");
        itemGrid.addColumn(Item::getDescription)
                .setSortable(true)
                .setAutoWidth(true)
                .setResizable(true)
                .setFlexGrow(1)
                .setHeader("Descripcion");
        itemGrid.addColumn(new ComponentRenderer<>(this::createButtonDeleteItem))
                .setFlexGrow(0)
                .setAutoWidth(true);

        layout.add(btnAdd,itemGrid);
        layout.setHorizontalComponentAlignment(FlexComponent.Alignment.END,btnAdd);

        return layout;
    }

    private Component createButtonDeleteItem(Item item){
        Button btn = new Button();
        btn.setIcon(VaadinIcon.TRASH.create());
        btn.addThemeVariants(ButtonVariant.LUMO_ERROR,ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_SMALL);
        Tooltips.getCurrent().setTooltip(btn,"Eliminar");
        btn.addClickListener(event -> {
            itemList.remove(item);
            itemGrid.getDataProvider().refreshAll();
            footer.saveState(true); //TODO HABILITAR SI TIENE EL ROL
        });

        return btn;
    }

//***    END ITEMS


//      INFORMATION QUOTE
    private DetailsDrawer createInformationQuote(Acquisition acquisition){

        DatePicker quotationRequestDate = new DatePicker("Fecha de solicitud de cotizaciones");
        quotationRequestDate.setWidth("30%");
        quotationRequestDate.setRequired(true);
        quotationRequestDate.setLocale(new Locale("es","BO"));

        DatePicker quotationReceptionDate = new DatePicker("Fecha de recepción de cotizaciones");
        quotationReceptionDate.setWidth("30%");
        quotationReceptionDate.setRequired(true);
        quotationReceptionDate.setLocale(new Locale("es","BO"));

//        binderQuoationRequest = new BeanValidationBinder<>(Acquisition.class);
        if(current.getAcquisitionNumber()!=null) {
            binder.forField(quotationRequestDate)
                    .asRequired("Fecha solicitud cotización es requerida")
                    .bind(Acquisition::getQuotationRequestDate, Acquisition::setQuotationRequestDate);
            binder.forField(quotationReceptionDate)
                    .asRequired(("Fecha de recepción de contizaciones es requerida"))
                    .bind(Acquisition::getQuotationReceptionDate, Acquisition::setQuotationReceptionDate);
            binder.addStatusChangeListener(event ->{
                boolean isValid = !event.hasValidationErrors();
                boolean hasChanges = binder.hasChanges();
                footer.saveState(isValid && hasChanges);
            });
        }
        VerticalLayout layout = new VerticalLayout();
        layout.setWidthFull();
        layout.add(quotationRequestDate,quotationReceptionDate);
        layout.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER,quotationRequestDate,quotationReceptionDate);

        DetailsDrawer detailsDrawer = new DetailsDrawer(DetailsDrawer.Position.BOTTOM);
        detailsDrawer.setWidthFull();
        detailsDrawer.setHeight("90%");

        detailsDrawer.setPadding(Left.S, Right.S, Top.S);
        detailsDrawer.setContent(layout);
//        detailsDrawer.setFooter(footer);
        detailsDrawer.show();


        return detailsDrawer;
    }

//    INFORMATION CAABS

    private DetailsDrawer createInformationCaabs(Acquisition acquisition){
        IntegerField caabsNumber = new IntegerField();
        caabsNumber.setWidthFull();
        caabsNumber.setMin(0);

        ComboBox<String> currency = new ComboBox<>();
        currency.setWidthFull();
        currency.setAllowCustomValue(false);
        currency.setAutoOpen(true);
        currency.setRequired(true);
        currency.setItems(utilValues.getValueParameterByCategory("MONEDA"));

        NumberField amount = new NumberField();
        amount.setMin(0.0);
        amount.setWidthFull();
        amount.setClearButtonVisible(true);
        amount.setRequiredIndicatorVisible(true);

        if(current.getQuotationReceptionDate()!=null){
            binder.forField(caabsNumber)
                    .asRequired("Numero CAABS es requerido")
                    .bind(Acquisition::getCaabsNumber,Acquisition::setCaabsNumber);
            binder.forField(currency)
                    .asRequired("Moneda es requerida")
                    .bind(Acquisition::getCurrency,Acquisition::setCurrency);
            binder.forField(amount)
                    .asRequired("Monto es requerido")
                    .withValidator(a -> a.doubleValue()>0.0,"Monto tiene que se mayor a 0")
                    .bind(Acquisition::getAmount,Acquisition::setAmount);
            binder.addStatusChangeListener(event ->{
                boolean isValid = !event.hasValidationErrors();
                boolean hasChanges = binder.hasChanges();
                footer.saveState(isValid && hasChanges);
            });

        }

        FormLayout form = new FormLayout();
        form.setWidthFull();
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

        form.addFormItem(caabsNumber,"Número de CAABS");
        form.addFormItem(currency,"Moneda");
        form.addFormItem(amount,"Monto");

        DetailsDrawer detailsDrawer = new DetailsDrawer(DetailsDrawer.Position.BOTTOM);
        detailsDrawer.setWidthFull();
        detailsDrawer.setHeight("90%");

        detailsDrawer.setPadding(Left.S, Right.S, Top.S);
        detailsDrawer.setContent(form, gridItems());
        detailsDrawer.show();


        return detailsDrawer;
    }



//    END INFORMATION CAABS

//    EXPENSE DISTRIBUITE

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

//      END EXPENSEDISTRIBUITE

//
}
