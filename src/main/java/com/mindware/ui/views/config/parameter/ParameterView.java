package com.mindware.ui.views.config.parameter;

import com.mindware.backend.entity.config.Parameter;
import com.mindware.backend.rest.parameter.ParameterRestTemplate;
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

@Route(value = "parameter", layout = MainLayout.class)
@ParentLayout(MainLayout.class)
@PageTitle("Parametros")
public class ParameterView extends SplitViewFrame implements RouterLayout {

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
    private TextField txtDetailsFilter;

    private Binder<Parameter> binder;
    private ListDataProvider<Parameter> dataProvider;

    private Parameter current;

    private String[] param = {"ACTIVIDAD","CARGOS","CATEGORIA TIPO CAMBIO","CODIGO CARGOS","MONEDA", "FRECUENCIA PAGO",
            "MONTO AUTORIZACION","OBLIGACIONES Y POLIZAS","OFICINAS","PERIODO",
            "RUBRO","SERVICIOS RECURRENTES", "TIPO ADQUISICION", "TIPO SERVICIO BASICO", "TIPO SOCIEDAD"};

    @Override
    protected void onAttach(AttachEvent attachEvent){
        super.onAttach(attachEvent);
        getListParameter();
        setViewHeader(createTopBar());
        setViewContent(createContent());
        setViewDetails(createDetailsDrawer());
    }

    private HorizontalLayout createTopBar(){
        btnNew = new Button("Nuevo parametro");
        btnNew.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnNew.setIcon(VaadinIcon.PLUS_CIRCLE.create());
        btnNew.addClickShortcut(Key.KEY_N, KeyModifier.ALT);
//        btnNew.setEnabled(GrantOptions.grantedOption("Parametros"));
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
        detailsDrawerHeader.setTitle("Parametro: ".concat(parameter.getValue()==null?"Nuevo":parameter.getValue()));
        detailsDrawer.setContent(createDetails(parameter));
        detailsDrawer.show();
        binder.readBean(parameter);
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
                Parameter result = restTemplate.add(current);
                if (current.getId()==null){
                    parameterList.add(result);
                    grid.getDataProvider().refreshAll();
                }else{
                    grid.getDataProvider().refreshItem(current);
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

    private Grid createGridParameter(){

        grid = new Grid<>();
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
                .setHeader("Categoria").setSortable(true).setKey("category").setResizable(true)
                .setAutoWidth(true).setTextAlign(ColumnTextAlign.START);
        grid.addColumn(Parameter::getValue)
                .setFlexGrow(1).setKey("value")
                .setHeader("Valor").setSortable(true).setFrozen(false).setResizable(true)
                .setAutoWidth(true).setTextAlign(ColumnTextAlign.START);
        grid.addColumn(Parameter::getDetails)
                .setFlexGrow(1).setKey("details")
                .setHeader("Detalle").setAutoWidth(true).setResizable(true)
                .setTextAlign(ColumnTextAlign.START);
        grid.addColumn(new ComponentRenderer<>(this::createButtonEdit))
                .setAutoWidth(true)
                .setFlexGrow(0);

        HeaderRow hr = grid.appendHeaderRow();

        cmbCategoryFilter = new ComboBox<>();
        cmbCategoryFilter.setItems(param);
        cmbCategoryFilter.setWidth("100%");
        cmbCategoryFilter.addValueChangeListener(e ->{
            applyFilter(dataProvider);
        });
        hr.getCell(grid.getColumnByKey("category")).setComponent(cmbCategoryFilter);

        txtValueFilter = new TextField();
        txtValueFilter.setValueChangeMode(ValueChangeMode.EAGER);
        txtValueFilter.setWidth("100%");
        txtValueFilter.addValueChangeListener(e ->{
            applyFilter(dataProvider);
        });
        hr.getCell(grid.getColumnByKey("value")).setComponent(txtValueFilter);


        txtDetailsFilter = new TextField();
        txtDetailsFilter.setValueChangeMode(ValueChangeMode.EAGER);
        txtDetailsFilter.setWidth("100%");
        txtDetailsFilter.addValueChangeListener(e ->{

            applyFilter(dataProvider);
        });
        hr.getCell(grid.getColumnByKey("details")).setComponent(txtDetailsFilter);

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

        ComboBox<String> cmbCategory = new ComboBox<>();
        cmbCategory.setItems(param);
        cmbCategory.setWidth("100%");
        cmbCategory.setRequired(true);

        TextField txtValue = new TextField();
        txtValue.setRequired(true);
        txtValue.setWidth("100%");

        TextArea txtDescription = new TextArea();
        txtDescription.setRequired(true);
        txtDescription.setWidth("100%");


        binder = new BeanValidationBinder<>(Parameter.class);

        binder.forField(cmbCategory).asRequired("Categoria es requerida").bind(Parameter::getCategory,Parameter::setCategory);
        binder.forField(txtValue).asRequired("Valor es requerido").bind(Parameter::getValue,Parameter::setValue);
        binder.forField(txtDescription).asRequired("Descripcion es requerida").bind(Parameter::getDetails,Parameter::setDetails);


        binder.addStatusChangeListener(event ->{
            boolean isValid = !event.hasValidationErrors();
            boolean hasChanges = binder.hasChanges();
//            footer.saveState(hasChanges && isValid && GrantOptions.grantedOption("Parametros"));
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
        form.addFormItem(cmbCategory,"Categoria");
        form.addFormItem(txtValue,"Valor");
        FormLayout.FormItem descriptionItem = form.addFormItem(txtDescription,"Descripcion");
        UIUtils.setColSpan(2,descriptionItem);
        return form;

    }


    private void getListParameter(){
        parameterList = new ArrayList<>(restTemplate.getAll());
        dataProvider = new ListDataProvider<>(parameterList);
    }

    private void applyFilter(ListDataProvider<Parameter> dataProvider){
        dataProvider.clearFilters();
        if (cmbCategoryFilter.getValue()!=null){
            dataProvider.addFilter(parameter -> Objects.equals(cmbCategoryFilter.getValue(),parameter.getCategory()));
        }
        if(!txtValueFilter.getValue().trim().equals("")){
//            dataProvider.addFilter(parameter -> Objects.equals(txtValueFilter.getValue(),parameter.getValue()));
            dataProvider.addFilter(parameter -> StringUtils.containsIgnoreCase(parameter.getValue(),txtValueFilter.getValue()));
        }
        if(!txtDetailsFilter.getValue().trim().equals("")){
//            dataProvider.addFilter(parameter -> Objects.equals(txtDescriptionFilter.getValue(),parameter.getDescription()));
            dataProvider.addFilter(parameter -> StringUtils.containsIgnoreCase(parameter.getDetails(), txtDetailsFilter.getValue()));
        }


    }
}
