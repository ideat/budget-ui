package com.mindware.ui.views.config.parameter;

import com.mindware.backend.entity.config.Parameter;
import com.mindware.backend.rest.parameter.ParameterRestTemplate;
import com.mindware.backend.util.GrantOptions;
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
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;
import dev.mett.vaadin.tooltip.Tooltips;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Route(value = "parameter-obligations", layout = MainLayout.class)
@ParentLayout(MainLayout.class)
@PageTitle("Parametros Obligaciones")
public class ParameterObligationsView extends SplitViewFrame implements RouterLayout {

    @Autowired
    private ParameterRestTemplate restTemplate;

    private List<Parameter> parameterList = new ArrayList<>();

    private DetailsDrawer detailsDrawer;
    private DetailsDrawerHeader detailsDrawerHeader;
    private DetailsDrawerFooter footer;

    private Button btnNew;
    private Grid<Parameter> grid;
    private ComboBox<String> cmbCategoryFilter;
    private TextField txtValueFilter;
    private TextField txtFrecuencyFilter;

    private Binder<Parameter> binder;
    private ListDataProvider<Parameter> dataProvider;

    private Parameter current;

//    private String[] param = {"ACTIVIDAD","CARGOS","CATEGORIA TIPO CAMBIO","CODIGO CARGOS","MONEDA", "FRECUENCIA PAGO",
//            "MONTO AUTORIZACION", "NIVELES AUTORIZACION", /*"OBLIGACIONES Y POLIZAS",*/"OFICINAS","PERIODO",
//            "RUBRO","SERVICIOS RECURRENTES", "TIPO ADQUISICION", "TIPO CUENTA","TIPO OBLIGACION", "TIPO SERVICIO BASICO", "TIPO SOCIEDAD"};

    @Override
    protected void onAttach(AttachEvent attachEvent){
        super.onAttach(attachEvent);
        getListParameter();
        setViewHeader(createTopBar());
        setViewContent(createContent());
        setViewDetails(createDetailsDrawer());
    }

    private HorizontalLayout createTopBar(){
        btnNew = new Button("Nuevo Tipo Obligación");
        btnNew.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnNew.setIcon(VaadinIcon.PLUS_CIRCLE.create());
        btnNew.addClickShortcut(Key.KEY_N, KeyModifier.ALT);
        btnNew.setEnabled(GrantOptions.grantedOptionWrite("Parametro Tipo Obligación"));
        btnNew.addClickListener(e -> {
            showDetails(new Parameter());
        });

        HorizontalLayout topLayout = new HorizontalLayout();
        topLayout.setWidth("100%");
        topLayout.add(btnNew);
        topLayout.setVerticalComponentAlignment(FlexComponent.Alignment.END,btnNew);
        topLayout.setSpacing(true);
        topLayout.setPadding(true);

        return topLayout;
    }

    private Component createContent(){
        FlexBoxLayout content = new FlexBoxLayout(createGridParameter());
        content.addClassName("grid-view");
        content.setHeightFull();
        content.setBoxSizing(BoxSizing.BORDER_BOX);
        content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);

        return content;
    }

    private void showDetails(Parameter parameter){
        current = parameter;
        detailsDrawerHeader.setTitle("Tipo Obligación: ".concat(parameter.getValue()==null?"Nuevo":parameter.getValue()));
        detailsDrawer.setContent(createDetails(parameter));
        detailsDrawer.show();

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
                try {
                    Parameter result = restTemplate.add(current);
                    if (current.getId() == null) {
                        parameterList.add(result);
                        grid.getDataProvider().refreshAll();
                    } else {
                        grid.getDataProvider().refreshItem(current);
                    }
                    detailsDrawer.hide();
                }catch (Exception ex){
                    UIUtils.showNotificationType(String.format("Tipo Obligación '%s', ya se encuentra registrada",current.getValue()),"alert");
                    return;
                }
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

    private Grid createGridParameter(){

        grid = new Grid<>();
        grid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_WRAP_CELL_CONTENT);
        grid.setId("parameter");
        grid.setMultiSort(true);
        grid.setHeightFull();
        grid.setWidthFull();
//
//        grid.addSelectionListener(event -> event.getFirstSelectedItem()
//                .ifPresent(this::showDetails));

        grid.setDataProvider(dataProvider);

        grid.addColumn(Parameter::getCategory)
                .setFlexGrow(0).setFrozen(false)
                .setHeader("Categoría").setSortable(true).setKey("category").setResizable(true)
                .setAutoWidth(true).setTextAlign(ColumnTextAlign.START);
        grid.addColumn(Parameter::getValue)
                .setFlexGrow(1).setKey("value")
                .setHeader("Valor").setSortable(true).setFrozen(false).setResizable(true)
                .setAutoWidth(true).setTextAlign(ColumnTextAlign.START);
        grid.addColumn(Parameter::getDetails)
                .setFlexGrow(1).setKey("frecuency")
                .setHeader("Frecuencia").setAutoWidth(true).setResizable(true)
                .setTextAlign(ColumnTextAlign.START);
        grid.addColumn(new ComponentRenderer<>(this::createButtonEdit))
                .setAutoWidth(true)
                .setFlexGrow(0);

//        HeaderRow hr = grid.appendHeaderRow();
//
//        cmbCategoryFilter = new ComboBox<>();
//        cmbCategoryFilter.setItems("MENSUAL","ANUAL");
//        cmbCategoryFilter.setWidth("100%");
//        cmbCategoryFilter.addValueChangeListener(e ->{
//            applyFilter(dataProvider);
//        });
//        hr.getCell(grid.getColumnByKey("category")).setComponent(cmbCategoryFilter);
//
//        txtValueFilter = new TextField();
//        txtValueFilter.setValueChangeMode(ValueChangeMode.EAGER);
//        txtValueFilter.setWidth("100%");
//        txtValueFilter.addValueChangeListener(e ->{
//            applyFilter(dataProvider);
//        });
//        hr.getCell(grid.getColumnByKey("value")).setComponent(txtValueFilter);
//
//
//        txtFrecuencyFilter = new TextField();
//        txtFrecuencyFilter.setValueChangeMode(ValueChangeMode.EAGER);
//        txtFrecuencyFilter.setWidth("100%");
//        txtFrecuencyFilter.addValueChangeListener(e ->{
//
//            applyFilter(dataProvider);
//        });
//        hr.getCell(grid.getColumnByKey("details")).setComponent(txtFrecuencyFilter);

        return grid;
    }

    private Component createButtonEdit(Parameter parameter){
        Button btn = new Button();
        Tooltips.getCurrent().setTooltip(btn,"Editar Registro");
        btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_SUCCESS);
        btn.setIcon(VaadinIcon.EDIT.create());
        btn.addClickListener(event -> {
            showDetails(parameter);
        });
        return btn;
    }

    private FormLayout createDetails(Parameter parameter){

        TextField txtCategory = new TextField();
        txtCategory.setWidth("100%");
        txtCategory.setRequired(true);
        txtCategory.setValue("TIPO OBLIGACION");
        txtCategory.setReadOnly(true);


        TextField txtNameTypeObligation = new TextField();
        txtNameTypeObligation.setRequired(true);
        txtNameTypeObligation.setWidth("100%");

        ComboBox<String> cmbFrecuency = new ComboBox<>();
        cmbFrecuency.setRequired(true);
        cmbFrecuency.setItems("MENSUAL","ANUAL");
        cmbFrecuency.setWidth("100%");

        binder = new BeanValidationBinder<>(Parameter.class);

        binder.forField(txtCategory)
                .asRequired("Categoría es requerida")
                .withConverter(new UtilValues.StringTrimValue())
                .bind(Parameter::getCategory,Parameter::setCategory);
        binder.forField(txtNameTypeObligation)
                .asRequired("Nombre Tipo Obligación es requerido")
                .withConverter(new UtilValues.StringTrimValue())
                .bind(Parameter::getValue,Parameter::setValue);
        binder.forField(cmbFrecuency)
                .asRequired("Frecuencia es requerida")
                .withConverter(new UtilValues.StringTrimValue())
                .bind(Parameter::getDetails,Parameter::setDetails);


        binder.addStatusChangeListener(event ->{
            boolean isValid = !event.hasValidationErrors();
            boolean hasChanges = binder.hasChanges();
            footer.saveState(hasChanges && isValid && GrantOptions.grantedOptionWrite("Parametro Tipo Obligación"));
        });

        FormLayout form = new FormLayout();
        form.addClassNames(LumoStyles.Padding.Bottom.L,
                LumoStyles.Padding.Horizontal.S, LumoStyles.Padding.Top.S);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1,
                        FormLayout.ResponsiveStep.LabelsPosition.TOP),
                new FormLayout.ResponsiveStep("21em", 2,
                        FormLayout.ResponsiveStep.LabelsPosition.TOP));
        form.addFormItem(txtCategory,"Categoría");
        form.addFormItem(txtNameTypeObligation,"Nombre Tipo Obligación");
        FormLayout.FormItem descriptionItem = form.addFormItem(cmbFrecuency,"Frecuencia");
        UIUtils.setColSpan(2,descriptionItem);
        binder.readBean(parameter);
        txtCategory.setValue("TIPO OBLIGACION");
        return form;

    }


    private void getListParameter(){
        parameterList = new ArrayList<>(restTemplate.getByCategory("TIPO OBLIGACION"));
        dataProvider = new ListDataProvider<>(parameterList);
    }

//    private void applyFilter(ListDataProvider<Parameter> dataProvider){
//        dataProvider.clearFilters();
//        if (cmbCategoryFilter.getValue()!=null){
//            dataProvider.addFilter(parameter -> StringUtils.containsIgnoreCase(cmbCategoryFilter.getValue(),parameter.getCategory()));
//        }
//        if(!txtValueFilter.getValue().trim().equals("")){
////            dataProvider.addFilter(parameter -> Objects.equals(txtValueFilter.getValue(),parameter.getValue()));
//            dataProvider.addFilter(parameter -> StringUtils.containsIgnoreCase(parameter.getValue(),txtValueFilter.getValue()));
//        }
//        if(!txtFrecuencyFilter.getValue().trim().equals("")){
////            dataProvider.addFilter(parameter -> Objects.equals(txtDescriptionFilter.getValue(),parameter.getDescription()));
//            dataProvider.addFilter(parameter -> StringUtils.containsIgnoreCase(parameter.getDetails(), txtFrecuencyFilter.getValue()));
//        }
//
//
//    }


}
