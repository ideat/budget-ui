package com.mindware.ui.views.contract;

import com.mindware.backend.entity.contract.Contract;
import com.mindware.backend.entity.contract.ContractDto;
import com.mindware.backend.entity.supplier.Supplier;
import com.mindware.backend.rest.contract.ContractDtoRestTemplate;
import com.mindware.backend.rest.contract.ContractRestTemplate;
import com.mindware.backend.rest.supplier.SupplierRestTemplate;
import com.mindware.backend.rest.typeChangeCurrency.TypeChangeCurrencyRestTemplate;
import com.mindware.backend.util.UtilValues;
import com.mindware.ui.MainLayout;
import com.mindware.ui.components.FlexBoxLayout;
import com.mindware.ui.components.detailsdrawer.DetailsDrawer;
import com.mindware.ui.components.detailsdrawer.DetailsDrawerFooter;
import com.mindware.ui.components.detailsdrawer.DetailsDrawerHeader;
import com.mindware.ui.layout.size.Horizontal;
import com.mindware.ui.layout.size.Left;
import com.mindware.ui.layout.size.Top;
import com.mindware.ui.util.LumoStyles;
import com.mindware.ui.util.UIUtils;
import com.mindware.ui.util.css.BoxSizing;
import com.mindware.ui.views.SplitViewFrame;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;
import dev.mett.vaadin.tooltip.Tooltips;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Route(value = "contract", layout = MainLayout.class)
@ParentLayout(MainLayout.class)
@PageTitle("Contratos")
public class ContractView extends SplitViewFrame implements RouterLayout {

    @Autowired
    ContractRestTemplate restTemplate;

    @Autowired
    ContractDtoRestTemplate contractDtoRestTemplate;

    @Autowired
    SupplierRestTemplate supplierRestTemplate;

    @Autowired
    TypeChangeCurrencyRestTemplate typeChangeCurrencyRestTemplate;

    @Autowired
    UtilValues utilValues;

    private List<ContractDto> contractDtoList;
//    private List<Contract> contractList;

    private Button btnNew;

    private ListDataProvider<ContractDto> dataProviderContractDto;

    private List<Supplier> supplierList;

    private DetailsDrawer detailsDrawer;
    private DetailsDrawerHeader detailsDrawerHeader;
    private DetailsDrawerFooter footer;
    private Binder<Contract> binder;
//    private ListDataProvider<Contract> contractDataProvider;

    private ComboBox<Supplier> supplier;
    private ComboBox<String> typeChangeCurrency;

    private Contract current;
    private  Grid<ContractDto> grid;

    private TextField supplierFilter;
    private DatePicker dateSubscriptionInitFilter;
    private DatePicker dateSubscriptionEndFilter;
    private TextField objectContractFilter;


    @Override
    protected void onAttach(AttachEvent attachEvent){
        super.onAttach(attachEvent);
        getContracts();
        setViewHeader(createTopBar());
        setViewContent(createContent());
        setViewDetails(createDetailsDrawer());

    }

    private FlexBoxLayout createTopBar(){
        btnNew = new Button("Nuevo Contrato");
        btnNew.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnNew.setIcon(VaadinIcon.PLUS_CIRCLE.create());
        btnNew.addClickShortcut(Key.KEY_N, KeyModifier.ALT);
        btnNew.addClickListener(e -> {
//            UI.getCurrent().navigate(SupplierRegisterView.class,"NUEVO");
            showDetails(new Contract());
        });

        FlexBoxLayout topLayout = new FlexBoxLayout();
        topLayout.setWidthFull();
        topLayout.add(btnNew);
        topLayout.setPadding(Left.L, Top.S);

        return topLayout;

    }

    private Component createContent() {
        FlexBoxLayout content = new FlexBoxLayout(createGridContract());
        content.addClassName("grid-view");
        content.setHeightFull();
        content.setBoxSizing(BoxSizing.BORDER_BOX);
        content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);

        return content;

    }

    private Grid createGridContract(){
        grid = new Grid();
        grid.setMultiSort(true);
        grid.setSizeFull();
        grid.setDataProvider(dataProviderContractDto);

        grid.addColumn(ContractDto::getSupplierName)
                .setFlexGrow(1)
                .setKey("name")
                .setHeader("Proveedor")
                .setSortable(true)
                .setAutoWidth(true)
                .setResizable(true);
        grid.addColumn(new LocalDateRenderer<>(ContractDto::getDateSubscription
                , DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .setKey("dateSubscription")
                .setHeader("Fecha Subscripción")
                .setFlexGrow(1)
                .setAutoWidth(true)
                .setSortable(true);
        grid.addColumn(ContractDto::getObjectContract)
                .setKey("objectContract")
                .setHeader("Objeto del Contrato")
                .setFlexGrow(1)
                .setAutoWidth(true)
                .setSortable(true);
        grid.addColumn(new ComponentRenderer<>(this::buttonEdit))
                .setAutoWidth(true);

        HeaderRow hr = grid.appendHeaderRow();

        supplierFilter = new TextField();
        supplierFilter.setValueChangeMode(ValueChangeMode.EAGER);
        supplierFilter.setWidthFull();
        supplierFilter.addValueChangeListener(e -> applyFilter(dataProviderContractDto));
        hr.getCell(grid.getColumnByKey("name")).setComponent(supplierFilter);

        dateSubscriptionInitFilter = new DatePicker();
        dateSubscriptionInitFilter.setWidth("30%");
        dateSubscriptionInitFilter.setLocale(new Locale("es","BO"));
        dateSubscriptionInitFilter.setClearButtonVisible(true);
        dateSubscriptionInitFilter.setI18n(UIUtils.spanish());
        dateSubscriptionInitFilter.addValueChangeListener(e -> {
           applyFilter(dataProviderContractDto);
        });

        dateSubscriptionEndFilter = new DatePicker();
        dateSubscriptionEndFilter.setWidth("30%");
        dateSubscriptionEndFilter.setLocale(new Locale("es","BO"));
        dateSubscriptionEndFilter.setClearButtonVisible(true);
        dateSubscriptionEndFilter.setI18n(UIUtils.spanish());
        dateSubscriptionEndFilter.addValueChangeListener(e -> {
            applyFilter(dataProviderContractDto);
        });
        HorizontalLayout layoutFilterDate = new HorizontalLayout();
        layoutFilterDate.add(dateSubscriptionInitFilter,dateSubscriptionEndFilter);
        hr.getCell(grid.getColumnByKey("dateSubscription")).setComponent(layoutFilterDate);

        objectContractFilter = new TextField();
        objectContractFilter.setValueChangeMode(ValueChangeMode.EAGER);
        objectContractFilter.setWidthFull();
        objectContractFilter.addValueChangeListener(e -> applyFilter(dataProviderContractDto));
        hr.getCell(grid.getColumnByKey("objectContract")).setComponent(objectContractFilter);

        return grid;
    }

    private Button buttonEdit(ContractDto contractDto){
        Button btn = new Button();
        Tooltips.getCurrent().setTooltip(btn,"Editar Registro");
        btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_SUCCESS);
        btn.setIcon(VaadinIcon.EDIT.create());
        btn.addClickListener(buttonClickEvent -> {
            Contract contract = restTemplate.getById(contractDto.getIdContract().toString());
            showDetails(contract);
        });

       return btn;
    }

    private FormLayout createDetails(Contract contract){

        supplier = new ComboBox<>();
        supplier.setWidthFull();
        supplier.setRequired(true);
        supplier.setErrorMessage("Proveedor es requerido");
        supplier.setItems(supplierList);
        supplier.setItemLabelGenerator(Supplier::getName);
        if(contract.getId()!=null)
        supplier.setValue(supplierList.stream()
                .filter(sup -> sup.getId().equals(contract.getIdSupplier())).collect(Collectors.toList()).get(0));

        DatePicker dateSubscription = new DatePicker();
        dateSubscription.setLocale(new Locale("es","BO"));
        dateSubscription.setRequired(true);
        dateSubscription.setI18n(UIUtils.spanish());
        dateSubscription.setWidth("90%");

        ComboBox<String> currency = new ComboBox<>();
        currency.setWidth("40%");
        currency.setItems(utilValues.getValueParameterByCategory("MONEDA"));
        currency.setRequired(true);


        NumberField amount = new NumberField();
        amount.setWidth("55%");
        amount.setMin(0);
        amount.setRequiredIndicatorVisible(true);

        TextArea objectContract = new TextArea();
        objectContract.setWidthFull();
        objectContract.setRequired(true);

        TextArea observation = new TextArea();
        observation.setWidthFull();

        DatePicker startDate = new DatePicker();
        startDate.setLocale(new Locale("es","BO"));
        startDate.setRequired(true);
        startDate.setWidthFull();
        startDate.setI18n(UIUtils.spanish());
        startDate.addValueChangeListener(event -> binder.validate());

        DatePicker finishDate = new DatePicker();
        finishDate.setLocale(new Locale("es","BO"));
        finishDate.setWidthFull();
        finishDate.setI18n(UIUtils.spanish());
        finishDate.addValueChangeListener(event -> binder.validate());

        Checkbox tacitReductionClause = new Checkbox("Clausula Tácita Reducción");
        tacitReductionClause.setWidthFull();
        tacitReductionClause.setValue(false);

        Checkbox physical = new Checkbox("Físico");
        physical.setWidthFull();
        physical.setValue(false);

        Checkbox original = new Checkbox("Original");
        original.setWidthFull();
        original.setValue(false);

        Checkbox undefinedTime = new Checkbox("Indefinido");
        undefinedTime.setWidthFull();
        undefinedTime.setValue(false);

        ComboBox<String> paymentFrecuency = new ComboBox<>();
        paymentFrecuency.setWidthFull();
        paymentFrecuency.setRequired(true);
        paymentFrecuency.setItems(utilValues.getValueParameterByCategory("FRECUENCIA PAGO"));

        typeChangeCurrency = new ComboBox<>();
        typeChangeCurrency.setWidthFull();
        typeChangeCurrency.setItems(utilValues.getValueParameterByCategory("CATEGORIA TIPO CAMBIO"));
        typeChangeCurrency.setAllowCustomValue(false);
        typeChangeCurrency.setErrorMessage("Categoría Tipo Cambio es requerida");
        typeChangeCurrency.setRequired(true);


        binder = new BeanValidationBinder<>(Contract.class);


        binder.forField(dateSubscription)
                .asRequired("Fecha de Subscripción es requerida")
//                .withValidator(d -> (startDate.getValue()!=null && d.isBefore(startDate.getValue())),"Fecha de subscripción no puede ser posterior a la fecha de inicio")
                .bind(Contract::getDateSubscription,Contract::setDateSubscription);
        binder.forField(currency).asRequired("Moneda es requerida")
                .bind(Contract::getCurrency,Contract::setCurrency);
        binder.forField(amount).asRequired("Monto es requerido")
                .withValidator(mnt -> mnt.doubleValue()>0, "Monto debe ser mayor a 0")
                .bind(Contract::getAmount,Contract::setAmount);
        binder.forField(objectContract).asRequired("Objeto del Contrato es requerido")
                .bind(Contract::getObjectContract,Contract::setObjectContract);
        binder.forField(observation).bind(Contract::getObservation,Contract::setObservation);
        binder.forField(startDate).asRequired("Fecha de Inicio es requerida")
                .withValidator(d ->  finishDate.getValue()!=null && d.isBefore(finishDate.getValue()),"Fecha de Inicio no puede ser posterior a la Fecha de Finalización")
                .withValidator(d -> d.isAfter(dateSubscription.getValue()),"Fecha Inicio no puede ser anterior a la Fecha de Subscripción")
                .bind(Contract::getStartDate,Contract::setStartDate);
        binder.forField(finishDate)
                .withValidator(d -> startDate.getValue()!=null && d.isAfter(startDate.getValue()),"Fecha de Finalización no puede ser anterior a la Fecha de Inicio")
                .withValidator(d -> dateSubscription.getValue()!=null && d.isAfter(dateSubscription.getValue()),"Fecha de Finalización no puede ser anterior a la Fecha de Subscripción")
                .bind(Contract::getFinishDate,Contract::setFinishDate);

        binder.forField(physical).bind(Contract::getPhysical,Contract::setPhysical);
        binder.forField(original).bind(Contract::getOriginal,Contract::setOriginal);
        binder.forField(undefinedTime).bind(Contract::getUndefinedTime,Contract::setUndefinedTime);
        binder.forField(tacitReductionClause).bind(Contract::getTacitReductionClause,Contract::setTacitReductionClause);
        binder.forField(paymentFrecuency).asRequired("Frecuencia de Pago es requerida")
                .bind(Contract::getPaymentFrecuency,Contract::setPaymentFrecuency);
        binder.forField(typeChangeCurrency)
//                .asRequired("Categoría Tipo Cambio es requerida")
                .bind(Contract::getTypeChangeCurrency, Contract::setTypeChangeCurrency);
        binder.addStatusChangeListener(event ->{
            boolean isValid = !event.hasValidationErrors();
            boolean hasChanges = binder.hasChanges();
            footer.saveState(isValid && hasChanges);
        });

        FormLayout form = new FormLayout();
        form.addClassNames(LumoStyles.Padding.Bottom.L,
                LumoStyles.Padding.Horizontal.S, LumoStyles.Padding.Top.S);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1,
                        FormLayout.ResponsiveStep.LabelsPosition.TOP),
                new FormLayout.ResponsiveStep("21em", 2,
                        FormLayout.ResponsiveStep.LabelsPosition.TOP));

        FormLayout.FormItem supplierItem = form.addFormItem(supplier,"Proveedor");
        UIUtils.setColSpan(2,supplierItem);
        form.addFormItem(dateSubscription,"Fecha de Subscripción");
        HorizontalLayout layoutAmount = new HorizontalLayout();
        layoutAmount.add(currency,amount);
        layoutAmount.setSpacing(true);
        form.addFormItem(layoutAmount,"Monto");
        FormLayout.FormItem objectContractItem = form.addFormItem(objectContract,"Objeto Contrato");
        UIUtils.setColSpan(2,objectContractItem);
        FormLayout.FormItem observationItem = form.addFormItem(observation,"Observaciones");
        UIUtils.setColSpan(2,observationItem);
        form.addFormItem(startDate,"Fecha de Inicio");
        form.addFormItem(finishDate,"Fecha de Finalización");
        VerticalLayout layoutChecks = new VerticalLayout();
        layoutChecks.add(tacitReductionClause,physical,original,undefinedTime);
        layoutChecks.setSpacing(false);
        FormLayout.FormItem layoutChecksItem = form.addFormItem(layoutChecks,"");
        UIUtils.setColSpan(2,layoutChecksItem);
        form.addFormItem(paymentFrecuency,"Frecuencia Pago");

        form.addFormItem(typeChangeCurrency,"Categoría Tipo Cambio");

        return form;
    }

    private void showDetails(Contract contract){
        current = contract;

        detailsDrawer.setContent(createDetails(contract));
        detailsDrawerHeader.setTitle("Contrato: ".concat(supplier.getValue()==null?"Nuevo":supplier.getValue().getName()));
        detailsDrawer.show();
        binder.readBean(contract);
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
                if(current.getCurrency().equals("$us") && (current.getTypeChangeCurrency()==null || current.getTypeChangeCurrency().isEmpty())){
                    typeChangeCurrency.setInvalid(true);

                    return;
                }
                current.setIdSupplier(supplier.getValue().getId());
                Contract result = restTemplate.add(current);
                if (current.getId()==null){
                    ContractDto dto = contractDtoRestTemplate.getByIdContract(result.getId().toString());
                    contractDtoList.add(dto);
                    grid.getDataProvider().refreshAll();
                }else{
                    ContractDto dto = contractDtoRestTemplate.getByIdContract(result.getId().toString());
                    grid.getDataProvider().refreshItem(dto);
                }
                detailsDrawer.hide();
            }else{
                UIUtils.dialog("Datos incorrectos, verifique nuevamente","alert").open();
            }
        });

        footer.addCancelListener(e ->{
            footer.saveState(false);
            detailsDrawer.hide();
        });

        detailsDrawer.setFooter(footer);
        return detailsDrawer;
    }

    private void getContracts(){
        contractDtoList = new ArrayList<>(contractDtoRestTemplate.getAll()) ;
        supplierList = supplierRestTemplate.getAll();
        dataProviderContractDto = new ListDataProvider<>(contractDtoList);
    }

    private void applyFilter(ListDataProvider<ContractDto> dataProvider){
        dataProvider.clearFilters();
        if(!supplierFilter.getValue().trim().equals("")){
            dataProvider.addFilter(contractDto -> StringUtils.containsIgnoreCase(contractDto.getSupplierName(),supplierFilter.getValue()));
        }
        if(dateSubscriptionInitFilter.getValue()!=null){
            dataProvider.addFilter(contractDto -> contractDto.getDateSubscription().isAfter(dateSubscriptionInitFilter.getValue())
                    || contractDto.getDateSubscription().isEqual(dateSubscriptionInitFilter.getValue()));
        }
        if(dateSubscriptionEndFilter.getValue()!=null){
            dataProvider.addFilter(contractDto -> contractDto.getDateSubscription().isBefore(dateSubscriptionEndFilter.getValue())
                    || contractDto.getDateSubscription().isEqual(dateSubscriptionEndFilter.getValue()));
        }
        if(!objectContractFilter.getValue().trim().equals("")){
            dataProvider.addFilter(contractDto -> StringUtils.containsIgnoreCase(contractDto.getObjectContract(),objectContractFilter.getValue()));
        }
    }
}
