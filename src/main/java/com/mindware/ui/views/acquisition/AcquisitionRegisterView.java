package com.mindware.ui.views.acquisition;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.mindware.backend.Invoice;
import com.mindware.backend.entity.acquisitionAuthorizer.AcquisitionAuthorizer;
import com.mindware.backend.entity.adquisition.*;
import com.mindware.backend.entity.commonJson.ExpenseDistribuite;
import com.mindware.backend.entity.corebank.Concept;
import com.mindware.backend.entity.supplier.Supplier;
import com.mindware.backend.entity.user.UserLdapDto;
import com.mindware.backend.rest.acquisition.AcquisitionRestTemplate;
import com.mindware.backend.rest.acquisitionAuthorizer.AcquisitionAuthorizerRestTemplate;
import com.mindware.backend.rest.corebank.ConceptRestTemplate;
import com.mindware.backend.rest.dataLdap.DataLdapRestTemplate;
import com.mindware.backend.rest.supplier.SupplierRestTemplate;
import com.mindware.backend.util.UtilValues;
import com.mindware.ui.MainLayout;
import com.mindware.ui.components.FlexBoxLayout;
import com.mindware.ui.components.detailsdrawer.DetailsDrawer;
import com.mindware.ui.components.detailsdrawer.DetailsDrawerFooter;
import com.mindware.ui.components.detailsdrawer.DetailsDrawerHeader;
import com.mindware.ui.components.navigation.bar.AppBar;
import com.mindware.ui.layout.size.*;
import com.mindware.ui.util.FontSize;
import com.mindware.ui.util.LumoStyles;
import com.mindware.ui.util.TextColor;
import com.mindware.ui.util.UIUtils;
import com.mindware.ui.views.SplitViewFrame;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Label;
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
import dev.mett.vaadin.tooltip.Tooltips;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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

    @Autowired
    private AcquisitionAuthorizerRestTemplate acquisitionAuthorizerRestTemplate;

    @Autowired
    private SupplierRestTemplate supplierRestTemplate;

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
//  AutorizerLevel
    private DetailsDrawerFooter footerSelectedAuthorizer;
    private DetailsDrawer detailsDrawerSelectedAuthorizer;
    private DetailsDrawerHeader detailsDrawerHeaderSelectedAuthorizer;
    private Binder<SelectedAuthorizer> selectedAuthorizerBinder;
    private ListDataProvider<SelectedAuthorizer> selectedAuthorizerDataProvider;
    private SelectedAuthorizer currentSelectedAuthorizer;
    private List<SelectedAuthorizer> selectedAuthorizerList;
    private Grid<SelectedAuthorizer> selectedAuthorizerGrid;
    //AuthorizerLevel2
    private Binder<SelectedAuthorizer> selectedAuthorizerLevel2Binder;
    private ListDataProvider<SelectedAuthorizer> selectedAuthorizerLevel2DataProvider;
    private SelectedAuthorizer currentSelectedAuthorizerLevel2;
    private List<SelectedAuthorizer> selectedAuthorizerLevel2List;
    private Grid<SelectedAuthorizer> selectedAuthorizerLevel2Grid;

//  AdjudicationInformation
    private Binder<AdjudicationInfomation> adjudicationInfomationBinder;
    private AdjudicationInfomation currentAdjudicationInformation;
//  ReceptionInformation
    private Binder<ReceptionInformation> receptionInformationBinder;
    private ReceptionInformation currentReceptionInformation;
    private TextField nameBusinessUnitReceptionInformation;
    private IntegerField codeBusinessUnitReceptionInformation;
    private TextField receiveBy;

//    InvoiceInformation
    private DetailsDrawerFooter footerInvoiceInformation;
    private DetailsDrawer detailsDrawerInvoiceInformation;
    private DetailsDrawerHeader detailsDrawerHeaderInvoiceInformation;
    private Binder<InvoiceInformation> invoiceInformationBinder;
    private InvoiceInformation currentInvoiceInformation;
    private Grid<InvoiceInformation> invoiceInformationGrid;
    private ListDataProvider<InvoiceInformation> invoiceInformationDataProvider;
    private List<InvoiceInformation> invoiceInformationList;
    private TextField nameSupplierInvoiceInformation;
    private TextField idSupplierInvoiceInformation;
    private List<Concept> conceptList;
    private String title;

    private FlexBoxLayout contentPurchaseRequest;
    private FlexBoxLayout contentInformationQuote; //informacion sobre cotizacion
    private FlexBoxLayout contentInformationCaabs;
    private FlexBoxLayout contentAdjudicationInformation;
    private FlexBoxLayout contentReceptionInformation;
    private FlexBoxLayout contentInvoiceInformation;

//    Filter Concept Agency
    private TextField codeFilter;
    private TextField code2Filter;
    private TextField descriptionFilter;

//    Filter User Agency
    private TextField cnFilter;
    private TextField titleFilter;

    private String currentTab;

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

//            Concept concept = conceptList.stream()
//                    .filter(c -> String.valueOf(current.getCodeBusinessUnit()).equals(c.getCode2()))
//                    .findFirst().get();
//            businessUnit.setValue(concept.getDescription());

            itemList = mapper.readValue(current.getItems(), new TypeReference<List<Item>>() {});
            itemDataprovider = new ListDataProvider<>(itemList);

            selectedAuthorizerList = mapper.readValue(current.getAuthorizersLevel1(), new TypeReference<List<SelectedAuthorizer>>(){});
            selectedAuthorizerDataProvider = new ListDataProvider<>(selectedAuthorizerList);

            selectedAuthorizerLevel2List = mapper.readValue(current.getAuthorizersLevel2(), new TypeReference<List<SelectedAuthorizer>>(){});
            selectedAuthorizerLevel2DataProvider = new ListDataProvider<>(selectedAuthorizerLevel2List);

            contentPurchaseRequest = (FlexBoxLayout) createContent(createPurchaseRequest(current));
            contentInformationQuote = (FlexBoxLayout) createContent(createInformationQuote(current));
            contentInformationCaabs = (FlexBoxLayout) createContent(createInformationCaabs(current));
            currentAdjudicationInformation = mapper.readValue(current.getAdjudicationInformation()
                    , new TypeReference<AdjudicationInfomation>() {});
            contentAdjudicationInformation = (FlexBoxLayout) createContent(createAdjudicationInformation());

            currentReceptionInformation = mapper.readValue(current.getReceptionInformation(), new TypeReference<ReceptionInformation>(){});
            contentReceptionInformation = (FlexBoxLayout) createContent(createReceptionInformation());

            invoiceInformationList = mapper.readValue(current.getInvoiceInformation(), new TypeReference<List<InvoiceInformation>>(){});
            invoiceInformationDataProvider = new ListDataProvider(invoiceInformationList);
            contentInvoiceInformation = (FlexBoxLayout) createContent(createInvoiceInformation());

            setViewContent(contentPurchaseRequest,contentInformationQuote,contentInformationCaabs
                    ,contentAdjudicationInformation, contentReceptionInformation,contentInvoiceInformation);

        }else{
            current = new Acquisition();
            current.setAuthorizersLevel1("[]");
            current.setAuthorizersLevel2("[]");
            current.setExpenseDistribuite("[]");
            current.setItems("[]");
            current.setAdjudicationInformation("{}");
            current.setReceptionInformation("{}");
            current.setInvoiceInformation("[]");
            title = "Adquición";

            itemList = mapper.readValue(current.getItems(), new TypeReference<List<Item>>() {});
            itemDataprovider = new ListDataProvider<>(itemList);

            selectedAuthorizerList = mapper.readValue(current.getAuthorizersLevel1(), new TypeReference<List<SelectedAuthorizer>>(){});
            selectedAuthorizerDataProvider = new ListDataProvider<>(selectedAuthorizerList);

            selectedAuthorizerLevel2List = mapper.readValue(current.getAuthorizersLevel2(), new TypeReference<List<SelectedAuthorizer>>(){});
            selectedAuthorizerLevel2DataProvider = new ListDataProvider<>(selectedAuthorizerLevel2List);

            contentPurchaseRequest = (FlexBoxLayout) createContent(createPurchaseRequest(current));
            contentInformationQuote = (FlexBoxLayout) createContent(createInformationQuote(current));
            contentInformationCaabs = (FlexBoxLayout) createContent(createInformationCaabs(current));
            currentAdjudicationInformation = mapper.readValue(current.getAdjudicationInformation(), new TypeReference<AdjudicationInfomation>() {});
            contentAdjudicationInformation = (FlexBoxLayout) createContent(createAdjudicationInformation());

            currentReceptionInformation = mapper.readValue(current.getReceptionInformation(), new TypeReference<ReceptionInformation>(){});
            contentReceptionInformation = (FlexBoxLayout) createContent(createReceptionInformation());

            invoiceInformationList = mapper.readValue(current.getInvoiceInformation(), new TypeReference<List<InvoiceInformation>>(){});
            invoiceInformationDataProvider = new ListDataProvider(invoiceInformationList);
            contentInvoiceInformation = (FlexBoxLayout) createContent(createInvoiceInformation());

            setViewContent(contentPurchaseRequest,contentInformationQuote,contentInformationCaabs,
                    contentAdjudicationInformation, contentReceptionInformation, contentInvoiceInformation);
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
                UIUtils.showNotificationType("Registre Items para su adquisición","alert");
                return;
            }
            if(adjudicationInfomationBinder!=null){
                if(adjudicationInfomationBinder.writeBeanIfValid(currentAdjudicationInformation)){
                    try {
                        String jsonAcquisitionInformation = mapper.writeValueAsString(currentAdjudicationInformation);
                        current.setAdjudicationInformation(jsonAcquisitionInformation);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }

                }else{
                    UIUtils.showNotificationType("Datos Información Adjudicacion no se completaron","alert");
                    return;
                }
            }

            if(receptionInformationBinder!=null){
                if(receptionInformationBinder.writeBeanIfValid(currentReceptionInformation)){
                    try {
                        String jsonReceptionInformation = mapper.writeValueAsString(currentReceptionInformation);
                        current.setReceptionInformation(jsonReceptionInformation);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                }else {
                    UIUtils.showNotificationType("Datos Recepción del Bien o Servicio no se completo", "alert");
                    return;
                }
            }

            if(invoiceInformationBinder!=null){
                try {
                    String jsonInvoiceInformation = mapper.writeValueAsString(invoiceInformationList);
                    current.setInvoiceInformation(jsonInvoiceInformation);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }

            if(binder.writeBeanIfValid(current)){
                try {
                    String jsonItems = mapper.writeValueAsString(itemList);
                    current.setItems(jsonItems);
                    String jsonAcquisitionAuthorizerLevel1 = mapper.writeValueAsString(selectedAuthorizerList);
                    String jsonAcquisitionAuthorizerLevel2 = mapper.writeValueAsString(selectedAuthorizerLevel2List);

                    current.setAuthorizersLevel1(jsonAcquisitionAuthorizerLevel1);
                    current.setAuthorizersLevel2(jsonAcquisitionAuthorizerLevel2);
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
        hideContent("");
        enabledSheets();
        appBar.addTabSelectionListener(e -> {
            enabledSheets();
            if(e.getSource().getSelectedTab()!=null){
                Tab selectedTab = appBar.getSelectedTab();
                hideContent(selectedTab.getLabel());
                currentTab = selectedTab.getLabel();

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
        if(current.getQuotationRequestDate()==null){
            contentInformationCaabs.setEnabled(false);
        }else{
            contentInformationCaabs.setEnabled(true);
        }
        if(current.getCaabsNumber()==null){
            contentAdjudicationInformation.setEnabled(false);
        }else{
            contentAdjudicationInformation.setEnabled(true);
        }
        if(current.getAdjudicationInformation()==null || current.getAdjudicationInformation().equals("{}")){
            contentReceptionInformation.setEnabled(false);
        }else{
            contentReceptionInformation.setEnabled(true);
        }
        if(current.getReceptionInformation()==null || current.getReceptionInformation().equals("[]")){
            contentInvoiceInformation.setEnabled(false);
        }else{
            contentInvoiceInformation.setEnabled(true);
        }
    }

    private void hideContent(String currentTab){
        contentPurchaseRequest.setVisible(true);
        contentInformationQuote.setVisible(false);
        contentInformationCaabs.setVisible(false);
        contentAdjudicationInformation.setVisible(false);
        contentReceptionInformation.setVisible(false);
        contentInvoiceInformation.setVisible(false);
        if(currentTab.equals("Solicitud de Compra")){
            contentPurchaseRequest.setVisible(true);
            contentInformationQuote.setVisible(false);
            contentInformationCaabs.setVisible(false);
            contentAdjudicationInformation.setVisible(false);
            contentReceptionInformation.setVisible(false);
            contentInvoiceInformation.setVisible(false);
        }else if(currentTab.equals("Información Cotización")){
            contentPurchaseRequest.setVisible(false);
            contentInformationQuote.setVisible(true);
            contentInformationCaabs.setVisible(false);
            contentAdjudicationInformation.setVisible(false);
            contentReceptionInformation.setVisible(false);
            contentInvoiceInformation.setVisible(false);
        }else if(currentTab.equals("Información CAABS")){
            contentPurchaseRequest.setVisible(false);
            contentInformationQuote.setVisible(false);
            contentInformationCaabs.setVisible(true);
            contentAdjudicationInformation.setVisible(false);
            contentReceptionInformation.setVisible(false);
            contentInvoiceInformation.setVisible(false);
        }else if(currentTab.equals("Información Adjudicación")){
            contentPurchaseRequest.setVisible(false);
            contentInformationQuote.setVisible(false);
            contentInformationCaabs.setVisible(false);
            contentAdjudicationInformation.setVisible(true);
            contentReceptionInformation.setVisible(false);
            contentInvoiceInformation.setVisible(false);
        }else if(currentTab.equals("Recepción del Bien o Servicio")){
            contentPurchaseRequest.setVisible(false);
            contentInformationQuote.setVisible(false);
            contentInformationCaabs.setVisible(false);
            contentAdjudicationInformation.setVisible(false);
            contentReceptionInformation.setVisible(true);
            contentInvoiceInformation.setVisible(false);
        }else if(currentTab.equals("Información de la Factura")){
            contentPurchaseRequest.setVisible(false);
            contentInformationQuote.setVisible(false);
            contentInformationCaabs.setVisible(false);
            contentAdjudicationInformation.setVisible(false);
            contentReceptionInformation.setVisible(false);
            contentInvoiceInformation.setVisible(true);
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
            if(currentTab.equals("Solicitud de Compra")) {
                codeBusinessUnit.setValue(Integer.valueOf(concept.getCode2()));
                businessUnit.setValue(concept.getDescription());
                applicant.clear();
                areaApplicant.clear();
            }else if(currentTab.equals("Recepción del Bien o Servicio")){

                nameBusinessUnitReceptionInformation.setValue(concept.getDescription());
                codeBusinessUnitReceptionInformation.setValue(Integer.valueOf(concept.getCode2()));
            }
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
            if(currentTab.equals("Solicitud de Compra")) {
                applicant.setValue(userLdapDto.getCn());
                areaApplicant.setValue(userLdapDto.getDepartament());
            }else if(currentTab.equals("Recepción del Bien o Servicio")){
               receiveBy.setValue(userLdapDto.getCn());
            }
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
        Label title2 = UIUtils.createLabel(FontSize.M, TextColor.SECONDARY,"Esta solicitud debe ser revisada por:");

        detailsDrawer.setContent(form, gridSelectedAuthorizer(), gridSelectedAuthorizerLevel2());
        detailsDrawer.show();


        return detailsDrawer;
    }

    private FormLayout layoutAuthorizersLevel(SelectedAuthorizer selectedAuthorizer){
        Concept concept = conceptList.stream()
                .filter(c -> String.valueOf(current.getCodeBusinessUnit()).equals(c.getCode2()))
                .findFirst().get();
        List<AcquisitionAuthorizer> acquisitionAuthorizerList = acquisitionAuthorizerRestTemplate
                .getByCodeBranchOffice(Integer.valueOf(concept.getCode()));

        TextField codePosition = new TextField();
        codePosition.setWidthFull();
        codePosition.setRequired(true);
        codePosition.setReadOnly(true);

        TextField nameBranchOffice = new TextField();
        nameBranchOffice.setWidthFull();
        nameBranchOffice.setRequired(true);
        nameBranchOffice.setReadOnly(true);

        ComboBox<String> fullName = new ComboBox<>();
        fullName.setRequired(true);
        fullName.setWidthFull();
        fullName.setAllowCustomValue(false);
        fullName.setItems(acquisitionAuthorizerList.stream()
                .map(AcquisitionAuthorizer::getFullName)
                .collect(Collectors.toList()));
        fullName.addValueChangeListener(event -> {
            AcquisitionAuthorizer acquisitionAuthorizer = acquisitionAuthorizerList.stream()
                    .filter(a -> event.getValue().equals(a.getFullName()))
                    .findFirst().get();
            codePosition.setValue(acquisitionAuthorizer.getCodePosition());
            nameBranchOffice.setValue(acquisitionAuthorizer.getNameBranchOffice());
        });

        DatePicker deliverDate = new DatePicker();
        deliverDate.setRequired(true);
        deliverDate.setWidthFull();
        deliverDate.setLocale(new Locale("es","BO"));

        DatePicker receptionDate = new DatePicker();
        receptionDate.setRequired(true);
        receptionDate.setWidthFull();
        receptionDate.setLocale(new Locale("es","BO"));

        selectedAuthorizerBinder = new BeanValidationBinder<>(SelectedAuthorizer.class);
        selectedAuthorizerBinder.forField(fullName)
                .asRequired("Autorizador es requerido")
                .bind(SelectedAuthorizer::getFullName,SelectedAuthorizer::setFullName);
        selectedAuthorizerBinder.forField(codePosition)
                .asRequired("Codigo Cargo es requerido")
                .bind(SelectedAuthorizer::getCodePosition,SelectedAuthorizer::setCodePosition);
        selectedAuthorizerBinder.forField(nameBranchOffice)
                .asRequired("Sucursal es requerida")
                .bind(SelectedAuthorizer::getNameBranchOffice,SelectedAuthorizer::setNameBranchOffice);
        selectedAuthorizerBinder.forField(deliverDate)
                .asRequired("Fecha de Entrega es requerida")
                .bind(SelectedAuthorizer::getDeliverDate,SelectedAuthorizer::setDeliverDate);
        selectedAuthorizerBinder.forField(receptionDate)
                .bind(SelectedAuthorizer::getReceptionDate,SelectedAuthorizer::setReceptionDate);

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
        FormLayout.FormItem nameBranchOfficeItem = form.addFormItem(nameBranchOffice,"Unidad Negocio");
        UIUtils.setColSpan(2,nameBranchOfficeItem);
        form.addFormItem(deliverDate,"Fecha Entrega");
        form.addFormItem(receptionDate,"Fecha Recepción");

        return form;
    }

    private void showDetailsSelectedAuthorizer(SelectedAuthorizer selectedAuthorizer){
        setViewDetails(createDetailsDrawerSelectedAuthorizer());
        setViewDetailsPosition(Position.RIGHT);
        currentSelectedAuthorizer = selectedAuthorizer;
        detailsDrawerHeaderSelectedAuthorizer.setTitle("Autorizador: "
                .concat(selectedAuthorizer.getFullName()==null?"Nuevo Autorizador":selectedAuthorizer.getFullName()));
        detailsDrawerSelectedAuthorizer.setContent(layoutAuthorizersLevel(currentSelectedAuthorizer));
        detailsDrawerSelectedAuthorizer.show();
    }

    private DetailsDrawer createDetailsDrawerSelectedAuthorizer(){
        detailsDrawerSelectedAuthorizer = new DetailsDrawer(DetailsDrawer.Position.RIGHT);

        detailsDrawerHeaderSelectedAuthorizer = new DetailsDrawerHeader("");
        detailsDrawerHeaderSelectedAuthorizer.addCloseListener(event -> detailsDrawerSelectedAuthorizer.hide());
        detailsDrawerSelectedAuthorizer.setHeader(detailsDrawerHeaderSelectedAuthorizer);

        footerSelectedAuthorizer = new DetailsDrawerFooter();
        footerSelectedAuthorizer.addSaveListener(e -> {
           if(currentSelectedAuthorizer !=null && selectedAuthorizerBinder.writeBeanIfValid(currentSelectedAuthorizer) ){

               selectedAuthorizerList.removeIf(sa -> sa.getId().equals(currentSelectedAuthorizer.getId()));
               currentSelectedAuthorizer.setId(UUID.randomUUID());
               selectedAuthorizerList.add(currentSelectedAuthorizer);
               detailsDrawerSelectedAuthorizer.hide();
               selectedAuthorizerGrid.getDataProvider().refreshAll();
               footerSelectedAuthorizer.saveState(true);
               footer.saveState(true);
           }
        });
        footerSelectedAuthorizer.addCancelListener(e -> detailsDrawerSelectedAuthorizer.hide());
        detailsDrawerSelectedAuthorizer.setFooter(footerSelectedAuthorizer);

        return detailsDrawerSelectedAuthorizer;
    }

    private VerticalLayout gridSelectedAuthorizer(){

        VerticalLayout layout = new VerticalLayout();
        layout.setWidthFull();
        Button btnAdd = new Button("Adicionar");
        btnAdd.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_CONTRAST,ButtonVariant.LUMO_SMALL);
        btnAdd.addClickListener(event -> {
            setViewDetailsPosition(Position.RIGHT);
            setViewDetails(createDetailsDrawerSelectedAuthorizer());
            showDetailsSelectedAuthorizer(new SelectedAuthorizer());
        });

        selectedAuthorizerGrid = new Grid<>();
        selectedAuthorizerGrid.setWidthFull();
        selectedAuthorizerGrid.setHeight("200px");
        selectedAuthorizerGrid.setDataProvider(selectedAuthorizerDataProvider);

        selectedAuthorizerGrid.addColumn(SelectedAuthorizer::getFullName)
                .setSortable(true)
                .setAutoWidth(true)
                .setResizable(true)
                .setFlexGrow(1)
                .setHeader("Autorizador");
        selectedAuthorizerGrid.addColumn(SelectedAuthorizer::getCodePosition)
                .setSortable(true)
                .setAutoWidth(true)
                .setResizable(true)
                .setFlexGrow(1)
                .setHeader("Cargo");
        selectedAuthorizerGrid.addColumn(SelectedAuthorizer::getNameBranchOffice)
                .setSortable(true)
                .setAutoWidth(true)
                .setResizable(true)
                .setFlexGrow(1)
                .setHeader("Unidad Negocio");
        selectedAuthorizerGrid.addColumn(new LocalDateRenderer<>(SelectedAuthorizer::getDeliverDate
                , DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .setSortable(true)
                .setAutoWidth(true)
                .setResizable(true)
                .setFlexGrow(1)
                .setHeader("Feche Entrega");
        selectedAuthorizerGrid.addColumn(new LocalDateRenderer<>(SelectedAuthorizer::getReceptionDate
                        , DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .setSortable(true)
                .setAutoWidth(true)
                .setResizable(true)
                .setFlexGrow(1)
                .setHeader("Feche Recepcion");
        selectedAuthorizerGrid.addColumn(new ComponentRenderer<>(this::createButtonDeleteSelectedAuthorizer))
                .setFlexGrow(1)
                .setAutoWidth(true);
        HorizontalLayout headerGrid = new HorizontalLayout();
        headerGrid.setWidthFull();
        Label title2 = UIUtils.createLabel(FontSize.L, TextColor.TERTIARY
                ,"Esta solicitud debe ser aprobada por: ");

        VerticalLayout layoutTitle = new VerticalLayout();
        layoutTitle.add(title2);
        layoutTitle.setHorizontalComponentAlignment(FlexComponent.Alignment.START,title2);
        VerticalLayout layoutButton = new VerticalLayout();
        layoutButton.add(btnAdd);
        layoutButton.setHorizontalComponentAlignment(FlexComponent.Alignment.END,btnAdd);
        headerGrid.add(layoutTitle,layoutButton);

        layout.add(headerGrid,selectedAuthorizerGrid);
//        layout.setHorizontalComponentAlignment(FlexComponent.Alignment.END,headerGrid);

        return layout;

    }

    private Component createButtonDeleteSelectedAuthorizer(SelectedAuthorizer selectedAuthorizer){
        Button btn = new Button();
        btn.setIcon(VaadinIcon.TRASH.create());
        btn.addThemeVariants(ButtonVariant.LUMO_ERROR,ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_SMALL);
        Tooltips.getCurrent().setTooltip(btn,"Eliminar");
        btn.addClickListener(event -> {
            selectedAuthorizerList.remove(selectedAuthorizer);
            selectedAuthorizerGrid.getDataProvider().refreshAll();
//            footerSelectedAuthorizer.saveState(true);
        });

        return btn;
    }

//    AuthorizerLevel 2

    private void showDetailsSelectedAuthorizerLevel2(SelectedAuthorizer selectedAuthorizer){
        setViewDetails(createDetailsDrawerSelectedAuthorizerLevel2());
        setViewDetailsPosition(Position.RIGHT);
        currentSelectedAuthorizerLevel2 = selectedAuthorizer;
        detailsDrawerHeaderSelectedAuthorizer.setTitle("Autorizador: "
                .concat(selectedAuthorizer.getFullName()==null?"Nuevo Autorizador":selectedAuthorizer.getFullName()));
        detailsDrawerSelectedAuthorizer.setContent(layoutAuthorizersLevel(currentSelectedAuthorizer));
        detailsDrawerSelectedAuthorizer.show();
    }

    private DetailsDrawer createDetailsDrawerSelectedAuthorizerLevel2(){
        detailsDrawerSelectedAuthorizer = new DetailsDrawer(DetailsDrawer.Position.RIGHT);

        detailsDrawerHeaderSelectedAuthorizer = new DetailsDrawerHeader("");
        detailsDrawerHeaderSelectedAuthorizer.addCloseListener(event -> detailsDrawerSelectedAuthorizer.hide());
        detailsDrawerSelectedAuthorizer.setHeader(detailsDrawerHeaderSelectedAuthorizer);

        footerSelectedAuthorizer = new DetailsDrawerFooter();
        footerSelectedAuthorizer.addSaveListener(e -> {
            if(currentSelectedAuthorizerLevel2 !=null && selectedAuthorizerBinder.writeBeanIfValid(currentSelectedAuthorizerLevel2) ){

                selectedAuthorizerLevel2List.removeIf(sa -> sa.getId().equals(currentSelectedAuthorizerLevel2.getId()));
                currentSelectedAuthorizerLevel2.setId(UUID.randomUUID());
                selectedAuthorizerLevel2List.add(currentSelectedAuthorizerLevel2);
                detailsDrawerSelectedAuthorizer.hide();
                selectedAuthorizerLevel2Grid.getDataProvider().refreshAll();
                footerSelectedAuthorizer.saveState(true);
                footer.saveState(true);
            }
        });
        footerSelectedAuthorizer.addCancelListener(e -> detailsDrawerSelectedAuthorizer.hide());
        detailsDrawerSelectedAuthorizer.setFooter(footerSelectedAuthorizer);

        return detailsDrawerSelectedAuthorizer;
    }

    private VerticalLayout gridSelectedAuthorizerLevel2(){

    VerticalLayout layout = new VerticalLayout();
    layout.setWidthFull();
    Button btnAdd = new Button("Adicionar");
      btnAdd.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_CONTRAST,ButtonVariant.LUMO_SMALL);
    btnAdd.addClickListener(event -> {
        setViewDetailsPosition(Position.RIGHT);
        setViewDetails(createDetailsDrawerSelectedAuthorizerLevel2());
        showDetailsSelectedAuthorizerLevel2(new SelectedAuthorizer());
    });

    selectedAuthorizerLevel2Grid = new Grid<>();
    selectedAuthorizerLevel2Grid.setWidthFull();
    selectedAuthorizerLevel2Grid.setHeight("200px");
    selectedAuthorizerLevel2Grid.setDataProvider(selectedAuthorizerLevel2DataProvider);

    selectedAuthorizerLevel2Grid.addColumn(SelectedAuthorizer::getFullName)
            .setSortable(true)
            .setAutoWidth(true)
            .setResizable(true)
            .setFlexGrow(1)
            .setHeader("Autorizador");
    selectedAuthorizerLevel2Grid.addColumn(SelectedAuthorizer::getCodePosition)
            .setSortable(true)
            .setAutoWidth(true)
            .setResizable(true)
            .setFlexGrow(1)
            .setHeader("Cargo");
    selectedAuthorizerLevel2Grid.addColumn(SelectedAuthorizer::getNameBranchOffice)
            .setSortable(true)
            .setAutoWidth(true)
            .setResizable(true)
            .setFlexGrow(1)
            .setHeader("Unidad Negocio");
    selectedAuthorizerLevel2Grid.addColumn(new LocalDateRenderer<>(SelectedAuthorizer::getDeliverDate
                    , DateTimeFormatter.ofPattern("dd/MM/yyyy")))
            .setSortable(true)
            .setAutoWidth(true)
            .setResizable(true)
            .setFlexGrow(1)
            .setHeader("Feche Entrega");
    selectedAuthorizerLevel2Grid.addColumn(new LocalDateRenderer<>(SelectedAuthorizer::getReceptionDate
                    , DateTimeFormatter.ofPattern("dd/MM/yyyy")))
            .setSortable(true)
            .setAutoWidth(true)
            .setResizable(true)
            .setFlexGrow(1)
            .setHeader("Feche Recepcion");
    selectedAuthorizerLevel2Grid.addColumn(new ComponentRenderer<>(this::createButtonDeleteSelectedAuthorizerLevel2))
            .setFlexGrow(1)
            .setAutoWidth(true);

        HorizontalLayout headerGrid = new HorizontalLayout();
        headerGrid.setWidthFull();
        Label title2 = UIUtils.createLabel(FontSize.L, TextColor.TERTIARY
                ,"Esta solicitud debe ser revisada por: ");

        VerticalLayout layoutTitle = new VerticalLayout();
        layoutTitle.add(title2);
        layoutTitle.setHorizontalComponentAlignment(FlexComponent.Alignment.START,title2);
        VerticalLayout layoutButton = new VerticalLayout();
        layoutButton.add(btnAdd);
        layoutButton.setHorizontalComponentAlignment(FlexComponent.Alignment.END,btnAdd);
        headerGrid.add(layoutTitle,layoutButton);

        layout.add(headerGrid,selectedAuthorizerLevel2Grid);

//    layout.add(btnAdd,selectedAuthorizerLevel2Grid);
//    layout.setHorizontalComponentAlignment(FlexComponent.Alignment.END,btnAdd);

    return layout;

}

    private Component createButtonDeleteSelectedAuthorizerLevel2(SelectedAuthorizer selectedAuthorizer){
        Button btn = new Button();
        btn.setIcon(VaadinIcon.TRASH.create());
        btn.addThemeVariants(ButtonVariant.LUMO_ERROR,ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_SMALL);
        Tooltips.getCurrent().setTooltip(btn,"Eliminar");
        btn.addClickListener(event -> {
            selectedAuthorizerLevel2List.remove(selectedAuthorizer);
            selectedAuthorizerLevel2Grid.getDataProvider().refreshAll();
//            footerSelectedAuthorizer.saveState(true);
        });

        return btn;
    }
//    End AuthorizerLevel2

//    END INFORMATION CAABS

//    ADJUDICATION INFORMATION

    private DetailsDrawer createAdjudicationInformation(){

        DatePicker  purchaseOrder = new DatePicker();
        purchaseOrder.setWidthFull();
        purchaseOrder.setRequired(true);
        purchaseOrder.setLocale(new Locale("es","BO"));

        IntegerField deliverTime = new IntegerField();
        deliverTime.setWidthFull();
        deliverTime.setMin(0);
        deliverTime.setRequiredIndicatorVisible(true);

        Checkbox requiresAdvance = new Checkbox("El proveedor solicita adelanto?");
        requiresAdvance.setWidthFull();

        Checkbox correspondsContract = new Checkbox("Corresponde contrato?");
        correspondsContract.setWidthFull();

        DatePicker requireUpdateDoc = new DatePicker();
        requireUpdateDoc.setWidthFull();
        requireUpdateDoc.setLocale(new Locale("es","BO"));

        DatePicker contractRequestDateToLegal = new DatePicker();
        contractRequestDateToLegal.setWidthFull();
        contractRequestDateToLegal.setLocale(new Locale("es","BO"));

        DatePicker contractDeliverContractFromLegal = new DatePicker();
        contractDeliverContractFromLegal.setWidthFull();
        contractDeliverContractFromLegal.setLocale(new Locale("es","BO"));

        DatePicker dateSignature = new DatePicker();
        dateSignature.setWidthFull();
        dateSignature.setLocale(new Locale("es","BO"));

        if(current.getCaabsNumber()!=null){
            adjudicationInfomationBinder = new BeanValidationBinder<>(AdjudicationInfomation.class);

            adjudicationInfomationBinder.forField(purchaseOrder)
                    .asRequired("Fecha de envio de la orden de compra es requerido")
                    .bind(AdjudicationInfomation::getPurchaseOrder,AdjudicationInfomation::setPurchaseOrder);
            adjudicationInfomationBinder.forField(deliverTime)
                    .asRequired("Tiempo de entrega es requerido")
                    .withValidator(d -> d.intValue()>0,"Tiempo entrega debe ser positivo")
                    .bind(AdjudicationInfomation::getDeliveryTime,AdjudicationInfomation::setDeliveryTime);
            adjudicationInfomationBinder.forField(requiresAdvance)
                    .bind(AdjudicationInfomation::isRequiresAdvance,AdjudicationInfomation::setRequiresAdvance);
            adjudicationInfomationBinder.forField(correspondsContract)
                    .bind(AdjudicationInfomation::isCorrespondsContract,AdjudicationInfomation::setCorrespondsContract);
            adjudicationInfomationBinder.forField(requireUpdateDoc)
                    .bind(AdjudicationInfomation::getRequireUpdateDoc,AdjudicationInfomation::setRequireUpdateDoc);
            adjudicationInfomationBinder.forField(contractRequestDateToLegal)
                    .bind(AdjudicationInfomation::getContractRequestDateToLegal,AdjudicationInfomation::setContractRequestDateToLegal);
            adjudicationInfomationBinder.forField(contractDeliverContractFromLegal)
                    .bind(AdjudicationInfomation::getContractDeliverContractFromLegal,AdjudicationInfomation::setContractDeliverContractFromLegal);
            adjudicationInfomationBinder.forField(dateSignature)
                    .bind(AdjudicationInfomation::getDateSignature,AdjudicationInfomation::setDateSignature);
            adjudicationInfomationBinder.addStatusChangeListener(event -> {
                boolean isValid = !event.hasValidationErrors();
                boolean hasChanges = adjudicationInfomationBinder.hasChanges();
                footer.saveState(isValid && hasChanges);
            });

            adjudicationInfomationBinder.readBean(currentAdjudicationInformation);
        }

        FormLayout form = new FormLayout();
        form.setWidth("30%");
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

        form.addFormItem(purchaseOrder,"Fecha de envío de la orden de compra");
        form.addFormItem(deliverTime,"Registro tiempo de entrega (días)");
        form.addFormItem(requiresAdvance,"");
        form.addFormItem(correspondsContract,"");
        form.addFormItem(requireUpdateDoc,"Fecha solicitud actualización documentos al proveedor");
        form.addFormItem(contractRequestDateToLegal,"Fecha solicitud contrato por legal");
        form.addFormItem(contractDeliverContractFromLegal,"Fecha entrega contrato por legal");
        form.addFormItem(dateSignature,"Fecha de firmas");

        VerticalLayout layout = new VerticalLayout();
        layout.setWidthFull();
        layout.add(form);
        layout.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER,form);

        DetailsDrawer detailsDrawer = new DetailsDrawer(DetailsDrawer.Position.BOTTOM);
        detailsDrawer.setWidthFull();
        detailsDrawer.setHeight("90%");

        detailsDrawer.setPadding(Left.M, Right.S, Top.S);
        detailsDrawer.setContent(layout);

        return detailsDrawer;
    }

//    END ADJUDICATION INFORMATION

//    RECEPTION INFORMATION

    private DetailsDrawer createReceptionInformation(){

        DatePicker dateReception = new DatePicker();
        dateReception.setWidthFull();
        dateReception.setRequired(true);
        dateReception.setLocale(new Locale("es","BO"));

        nameBusinessUnitReceptionInformation = new TextField();
        nameBusinessUnitReceptionInformation.setWidth("50%");
        nameBusinessUnitReceptionInformation.setReadOnly(true);
        nameBusinessUnitReceptionInformation.setRequired(true);

        codeBusinessUnitReceptionInformation = new IntegerField();
        codeBusinessUnitReceptionInformation.setRequiredIndicatorVisible(true);
        codeBusinessUnitReceptionInformation.setWidth("20%");
        codeBusinessUnitReceptionInformation.setReadOnly(true);

        receiveBy = new TextField();
        receiveBy.setWidthFull();
        receiveBy.setRequired(true);
        receiveBy.setReadOnly(true);

        if(current.getAdjudicationInformation()!=null && !current.getAdjudicationInformation().equals("{}")){
            receptionInformationBinder = new BeanValidationBinder<>(ReceptionInformation.class);

            receptionInformationBinder.forField(dateReception)
                    .asRequired("Fecha de recepción es requerida")
                    .bind(ReceptionInformation::getDateReception,ReceptionInformation::setDateReception);
            receptionInformationBinder.forField(nameBusinessUnitReceptionInformation)
                    .asRequired("Unidad Negocio es requerido")
                    .bind(ReceptionInformation::getNameBusinessUnit,ReceptionInformation::setNameBusinessUnit);
            receptionInformationBinder.forField(codeBusinessUnitReceptionInformation)
                    .asRequired("Codigo Unidad de Negocio es requerido")
                    .bind(ReceptionInformation::getCodeBusinessUnit,ReceptionInformation::setCodeBusinessUnit);
            receptionInformationBinder.forField(receiveBy)
                    .asRequired("Persona que firma la conformidad de recepción es requerida")
                    .bind(ReceptionInformation::getReceivedBy,ReceptionInformation::setReceivedBy);
            receptionInformationBinder.addStatusChangeListener(event -> {
                boolean isValid = !event.hasValidationErrors();
                boolean hasChanges = receptionInformationBinder.hasChanges();
                footer.saveState(isValid && hasChanges);
            });

            receptionInformationBinder.readBean(currentReceptionInformation);
        }

        FormLayout form = new FormLayout();
        form.setWidth("40%");
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

        form.addFormItem(dateReception,"Fecha de recepción");
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

        layoutBusinessUnit.add(codeBusinessUnitReceptionInformation,nameBusinessUnitReceptionInformation,btnSearchBusinessUnit);
        FormLayout.FormItem businessUnitItem = form.addFormItem(layoutBusinessUnit,"Unidad Negocio: Agencias y Sucursales");
        UIUtils.setColSpan(2,businessUnitItem);

        HorizontalLayout layoutApplicant = new HorizontalLayout();
        Button btnSearchApplicant = new Button();
        btnSearchApplicant.setWidth("10%");
        btnSearchApplicant.addThemeVariants(ButtonVariant.LUMO_SMALL,ButtonVariant.LUMO_PRIMARY);
        btnSearchApplicant.setIcon(VaadinIcon.SEARCH_PLUS.create());
        btnSearchApplicant.addClickListener(event -> {
            if(!codeBusinessUnitReceptionInformation.isEmpty()) {
                setViewDetails(createDetailDrawer());
                setViewDetailsPosition(Position.BOTTOM);
                showSearchApplicant();
            }else{
                UIUtils.showNotificationType("Seleccione una Unidad de Negocio", "alert");
            }
        });
        layoutApplicant.add(receiveBy,btnSearchApplicant);
        FormLayout.FormItem applicantItem = form.addFormItem(layoutApplicant,"Firma la conformidad de recepción");
        UIUtils.setColSpan(2,applicantItem);

        VerticalLayout layout = new VerticalLayout();
        layout.setWidthFull();
        layout.add(form);
        layout.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER,form);

        DetailsDrawer detailsDrawer = new DetailsDrawer(DetailsDrawer.Position.BOTTOM);
        detailsDrawer.setWidthFull();
        detailsDrawer.setHeight("90%");

        detailsDrawer.setPadding(Left.M, Right.S, Top.S);
        detailsDrawer.setContent(layout);

        return detailsDrawer;
    }

//    END RECEPTION INFORMATION

//    INVOICE INFORMATION

    private DetailsDrawer createInvoiceInformation(){

        VerticalLayout layout = new VerticalLayout();
        layout.setWidthFull();
        Button btnAdd = new Button("Adicionar");
        btnAdd.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_CONTRAST,ButtonVariant.LUMO_SMALL);
        btnAdd.addClickListener(event -> {
            setViewDetailsPosition(Position.RIGHT);
            setViewDetails(createDetailsDrawerInvoiceInformation());
            showDetailsInvoiceInformation(new InvoiceInformation());
        });

        invoiceInformationGrid = new Grid<>();

        invoiceInformationGrid.setDataProvider(invoiceInformationDataProvider);
        invoiceInformationGrid.addColumn(InvoiceInformation::getNameSupplier)
                .setSortable(true)
                .setAutoWidth(true)
                .setResizable(true)
                .setFlexGrow(1)
                .setHeader("Proveedor");
        invoiceInformationGrid.addColumn(InvoiceInformation::getInvoiceNumber)
                .setSortable(true)
                .setAutoWidth(true)
                .setResizable(true)
                .setFlexGrow(1)
                .setHeader("Número Factura");
        invoiceInformationGrid.addColumn(new LocalDateRenderer<>(InvoiceInformation::getDateInvoice
                , DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .setSortable(true)
                .setAutoWidth(true)
                .setResizable(true)
                .setFlexGrow(1)
                .setHeader("Fecha Factura");
        invoiceInformationGrid.addColumn(new NumberRenderer<>(InvoiceInformation::getAmount
                , " %(,.2f",
                Locale.US, "0.00"))
                .setSortable(true)
                .setAutoWidth(true)
                .setResizable(true)
                .setFlexGrow(1)
                .setHeader("Monto(Bs.)");
        invoiceInformationGrid.addColumn(new ComponentRenderer<>(this::createButtonDeleteInvoiceInformation))
                .setAutoWidth(true)
                .setFlexGrow(1);
        invoiceInformationGrid.addColumn(new ComponentRenderer<>(this::createButtonEditInvoiceInformation))
                .setAutoWidth(true)
                .setFlexGrow(1);

        layout.add(btnAdd,invoiceInformationGrid);
        layout.setHorizontalComponentAlignment(FlexComponent.Alignment.END,btnAdd);

        DetailsDrawer detailsDrawer = new DetailsDrawer(DetailsDrawer.Position.BOTTOM);
        detailsDrawer.setWidthFull();
        detailsDrawer.setHeight("90%");

        detailsDrawer.setPadding(Left.M, Right.S, Top.S);
        detailsDrawer.setContent(layout);

        return detailsDrawer;
    }

    private Component createButtonDeleteInvoiceInformation(InvoiceInformation invoiceInformation){
        Button btn = new Button();
        btn.setIcon(VaadinIcon.TRASH.create());
        btn.addThemeVariants(ButtonVariant.LUMO_ERROR,ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_SMALL);
        Tooltips.getCurrent().setTooltip(btn,"Eliminar");
        btn.addClickListener(event -> {
            invoiceInformationList.remove(invoiceInformation);
            invoiceInformationGrid.getDataProvider().refreshAll();
            footer.saveState(true);
        });

        return btn;
    }

    private Component createButtonEditInvoiceInformation(InvoiceInformation invoiceInformation){
        Button btn = new Button();
        btn.setIcon(VaadinIcon.EDIT.create());
        btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_SMALL);
        Tooltips.getCurrent().setTooltip(btn,"Editar");
        btn.addClickListener(event -> {
            setViewDetailsPosition(Position.RIGHT);
            setViewDetails(createDetailsDrawerInvoiceInformation());
            showDetailsInvoiceInformation(invoiceInformation);
        });

        return btn;
    }

    private void showDetailsInvoiceInformation(InvoiceInformation invoiceInformation){
        setViewDetails(createDetailsDrawerInvoiceInformation());
        setViewDetailsPosition(Position.RIGHT);
        currentInvoiceInformation = invoiceInformation;
        detailsDrawerHeaderInvoiceInformation.setTitle("Factura Nro: "
                .concat(invoiceInformation.getInvoiceNumber()==null?"Nuevo":invoiceInformation.getInvoiceNumber().toString()));
        detailsDrawerInvoiceInformation.setContent(layoutInvoiceInformation(currentInvoiceInformation));
        detailsDrawerInvoiceInformation.show();

    }

    private DetailsDrawer createDetailsDrawerInvoiceInformation(){
        detailsDrawerInvoiceInformation = new DetailsDrawer(DetailsDrawer.Position.RIGHT);

        detailsDrawerHeaderInvoiceInformation = new DetailsDrawerHeader("");
        detailsDrawerHeaderInvoiceInformation.addCloseListener(event -> detailsDrawerInvoiceInformation.hide());
        detailsDrawerInvoiceInformation.setHeader(detailsDrawerHeaderInvoiceInformation);

        footerInvoiceInformation = new DetailsDrawerFooter();
        footerInvoiceInformation.addSaveListener(e -> {
            if(currentInvoiceInformation != null && invoiceInformationBinder.writeBeanIfValid(currentInvoiceInformation)){
                invoiceInformationList.removeIf(ii -> ii.getId().equals(currentInvoiceInformation.getId()));
                currentInvoiceInformation.setId(UUID.randomUUID());
                invoiceInformationList.add(currentInvoiceInformation);
                detailsDrawerInvoiceInformation.hide();
                invoiceInformationGrid.getDataProvider().refreshAll();
                footerInvoiceInformation.saveState(true);
                footer.saveState(true);
            }
        });
        footerInvoiceInformation.addCancelListener(e -> detailsDrawerInvoiceInformation.hide());
        detailsDrawerInvoiceInformation.setFooter(footerInvoiceInformation);

        return detailsDrawerInvoiceInformation;
    }

    private FormLayout layoutInvoiceInformation(InvoiceInformation invoiceInformation){

        idSupplierInvoiceInformation = new TextField();
        idSupplierInvoiceInformation.setWidthFull();

        TextField nit = new TextField();
        nit.setWidthFull();
        nit.setErrorMessage("Ingrese NIT");

        nameSupplierInvoiceInformation = new TextField();
        nameSupplierInvoiceInformation.setRequired(true);
        nameSupplierInvoiceInformation.setWidthFull();
        nameSupplierInvoiceInformation.setReadOnly(true);


        DatePicker dateInvoice = new DatePicker();
        dateInvoice.setWidthFull();
        dateInvoice.setRequired(true);
        dateInvoice.setLocale(new Locale("es","BO"));

        NumberField amount = new NumberField();
        amount.setMin(0.0);
        amount.setWidthFull();
        amount.setRequiredIndicatorVisible(true);
        amount.setClearButtonVisible(true);

        IntegerField invoiceNumber = new IntegerField();
        invoiceNumber.setWidthFull();
        invoiceNumber.setClearButtonVisible(true);


        invoiceInformationBinder = new BeanValidationBinder<>(InvoiceInformation.class);
        invoiceInformationBinder.forField(idSupplierInvoiceInformation)
                        .bind(InvoiceInformation::getIdSupplier,InvoiceInformation::setIdSupplier);
        invoiceInformationBinder.forField(nameSupplierInvoiceInformation)
                .asRequired("Razon social del proveedor es requerida")
                .bind(InvoiceInformation::getNameSupplier,InvoiceInformation::setNameSupplier);
        invoiceInformationBinder.forField(dateInvoice)
                .asRequired("Fecha factura es requerida")
                .bind(InvoiceInformation::getDateInvoice,InvoiceInformation::setDateInvoice);
        invoiceInformationBinder.forField(amount)
                .asRequired("Monto factura es requerida")
                .withValidator(a -> a.doubleValue()>0.0,"Monto factura debe ser mayor a cero")
                .bind(InvoiceInformation::getAmount,InvoiceInformation::setAmount);
        invoiceInformationBinder.forField(invoiceNumber)
                .asRequired("Número factura es requerido")
                .withValidator(n -> n.intValue()>0,"Número factura debe ser mayor a cero")
                .bind(InvoiceInformation::getInvoiceNumber,InvoiceInformation::setInvoiceNumber);

        FormLayout form = new FormLayout();
        form.addClassNames(LumoStyles.Padding.Bottom.L,
                LumoStyles.Padding.Horizontal.S, LumoStyles.Padding.Top.S);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1,
                        FormLayout.ResponsiveStep.LabelsPosition.TOP),
                new FormLayout.ResponsiveStep("21em", 2,
                        FormLayout.ResponsiveStep.LabelsPosition.TOP));

        HorizontalLayout layoutNit = new HorizontalLayout();
        Button btnNit = new Button();
        btnNit.setWidth("10%");
        btnNit.addThemeVariants(ButtonVariant.LUMO_SMALL,ButtonVariant.LUMO_PRIMARY);
        btnNit.setIcon(VaadinIcon.SEARCH_PLUS.create());
        layoutNit.add(nit,btnNit);
        FormLayout.FormItem nitItem = form.addFormItem(layoutNit,"Buscar proveedor por NIT");
        UIUtils.setColSpan(2,nitItem);

        FormLayout.FormItem nameSupplierItem = form.addFormItem(nameSupplierInvoiceInformation,"Nombre Proveedor");
        UIUtils.setColSpan(2,nameSupplierItem);
        form.addFormItem(dateInvoice,"Fecha factura");
        form.addFormItem(amount,"Monto factura");
        form.addFormItem(invoiceNumber,"Número factura");

        btnNit.addClickListener(event -> {
            if(!nit.isEmpty()){
                nit.setInvalid(false);
                Supplier supplier = supplierRestTemplate.getByNit(nit.getValue());
                if(supplier.getId()==null){
                    UIUtils.showNotificationType("No existe Proveedor, registre el proveedor", "info");
                    Dialog dialog = new Dialog();
                    VerticalLayout dialogLayout = createDialogLayout(dialog,nit.getValue());
                    dialog.add(dialogLayout);
                    dialog.setModal(true);
                    dialog.open();
                }else{
                    idSupplierInvoiceInformation.setValue(supplier.getId().toString());
                    nameSupplierInvoiceInformation.setValue(supplier.getName());
                }
            }else{
               nit.setInvalid(true);

            }

        });
        invoiceInformationBinder.readBean(invoiceInformation);

        return form;

    }

    private VerticalLayout createDialogLayout(Dialog dialog,String nitValue) {
        H2 headline = new H2("Crear Nuevo Proveedor");
        headline.getStyle().set("margin", "var(--lumo-space-m) 0 0 0")
                .set("font-size", "1.5em").set("font-weight", "bold");

        TextField nit = new TextField("Número NIT");
        nit.setWidthFull();
        nit.setRequiredIndicatorVisible(true);
        nit.setRequired(true);
        nit.setReadOnly(true);
        nit.setValue(nitValue);

        TextField name = new TextField("Razon Social");
        name.setWidthFull();
        name.setRequired(true);

        TextField legalRepresentative = new TextField("Representante Legal");
        legalRepresentative.setRequired(true);
        legalRepresentative.setWidthFull();

        ComboBox<String> typeBusinessCompany = new ComboBox<>("Tipo Sociedad");
        typeBusinessCompany.setRequired(true);
        typeBusinessCompany.setItems(utilValues.getValueParameterByCategory("TIPO SOCIEDAD"));
        typeBusinessCompany.setAllowCustomValue(false);
        typeBusinessCompany.setAutoOpen(true);

        TextField address = new TextField("Direccion");
        address.setWidthFull();
        address.setRequired(true);


        VerticalLayout layout = new VerticalLayout(nit, name, legalRepresentative,typeBusinessCompany,address);
        layout.setSpacing(false);
        layout.setPadding(false);
        layout.setAlignItems(FlexComponent.Alignment.STRETCH);

        Binder<Supplier> supplierBinder = new BeanValidationBinder<>(Supplier.class);
        supplierBinder.forField(nit)
                .asRequired("NIT es requerido")
                .bind(Supplier::getNit,Supplier::setNit);
        supplierBinder.forField(name)
                .asRequired("Razon Social es requerida")
                .bind(Supplier::getName,Supplier::setName);
        supplierBinder.forField(legalRepresentative)
                .asRequired("Representante legal es requerido")
                .bind(Supplier::getLegalRepresentative, Supplier::setLegalRepresentative);
        supplierBinder.forField(typeBusinessCompany)
                .asRequired("Tipo sociedad es requerido")
                .bind(Supplier::getTypeBusinessCompany, Supplier::setTypeBusinessCompany);
        supplierBinder.forField(address)
                .asRequired("Direccion es requerida")
                .bind(Supplier::getAddress,Supplier::setAddress);


        Button cancelButton = new Button("Cancelar", e -> dialog.close());
        Button saveButton = new Button("Guardar", e -> {
            Supplier supplier = new Supplier();
            if(supplierBinder.writeBeanIfValid(supplier)){
                supplier.setPendingCompleting("NO");
                supplier = supplierRestTemplate.add(supplier);
                nameSupplierInvoiceInformation.setValue(name.getValue());
                idSupplierInvoiceInformation.setValue(supplier.getId().toString());
                dialog.close();
            }else{
                UIUtils.showNotificationType("Datos incompletos Registro Rapido de Proveedor", "alert");
            }

        } );
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        HorizontalLayout buttonLayout = new HorizontalLayout(cancelButton,
                saveButton);
        buttonLayout
                .setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        VerticalLayout dialogLayout = new VerticalLayout(headline, layout,
                buttonLayout);
        dialogLayout.setPadding(false);
        dialogLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
        dialogLayout.getStyle().set("width", "300px").set("max-width", "100%");

        return dialogLayout;
    }

//    END INVOICE INFORMATION
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
