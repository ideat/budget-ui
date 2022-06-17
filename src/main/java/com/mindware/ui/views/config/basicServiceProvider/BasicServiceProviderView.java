package com.mindware.ui.views.config.basicServiceProvider;

import com.mindware.backend.entity.config.BasicServiceProvider;
import com.mindware.backend.entity.config.Parameter;
import com.mindware.backend.entity.contract.Contract;
import com.mindware.backend.rest.basicServiceProvider.BasicServiceProviderRestTemplate;
import com.mindware.backend.util.GrantOptions;
import com.mindware.backend.util.UtilValues;
import com.mindware.ui.MainLayout;
import com.mindware.ui.components.FlexBoxLayout;
import com.mindware.ui.components.detailsdrawer.DetailsDrawer;
import com.mindware.ui.components.detailsdrawer.DetailsDrawerFooter;
import com.mindware.ui.components.detailsdrawer.DetailsDrawerHeader;
import com.mindware.ui.layout.size.Horizontal;
import com.mindware.ui.layout.size.Left;
import com.mindware.ui.layout.size.Right;
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
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.IntegerField;
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

import javax.websocket.RemoteEndpoint;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Route(value = "basic-service-provider", layout = MainLayout.class)
@ParentLayout(MainLayout.class)
@PageTitle("Proveedor de Servicios Basicos")
public class BasicServiceProviderView extends SplitViewFrame implements RouterLayout {

    @Autowired
    private BasicServiceProviderRestTemplate restTemplate;

    @Autowired
    private UtilValues utilValues;

    private DetailsDrawer detailsDrawer;
    private DetailsDrawerHeader detailsDrawerHeader;
    private DetailsDrawerFooter footer;

    private ListDataProvider<BasicServiceProvider> dataProvider;
    private Binder<BasicServiceProvider> binder;
    private List<BasicServiceProvider> basicServiceProviderList;

    private Grid<BasicServiceProvider> grid;

    private Button btnNew;
    private ComboBox<String> typeServiceFilter;
    private TextField providerFilter;
    private IntegerField nitFilter;
    private TextField descriptionFilter;

    private BasicServiceProvider current;
    private List<String> typeServiceList;

    @Override
    protected void onAttach(AttachEvent attachEvent){
        super.onAttach(attachEvent);
        getBasicServiceProvider();
        setViewHeader(createTopBar());
        setViewContent(createContent());
        setViewDetails(createDetailsDrawer());
    }

    private FlexBoxLayout createTopBar(){
        btnNew = new Button("Nuevo Proveedor");
        btnNew.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnNew.setIcon(VaadinIcon.PLUS_CIRCLE.create());
        btnNew.addClickShortcut(Key.KEY_N, KeyModifier.ALT);
        btnNew.setVisible(GrantOptions.grantedOptionWrite("Proveedor Serv. Básicos"));
        btnNew.addClickListener(e -> {
            showDetails(new BasicServiceProvider());
        });

        FlexBoxLayout layout = new FlexBoxLayout();
        layout.setWidthFull();
        layout.setFlexDirection(FlexLayout.FlexDirection.ROW);
        layout.setPadding(Left.L, Top.S);
        layout.add(btnNew);

        return layout;
    }

    private Component createContent(){
        FlexBoxLayout content = new FlexBoxLayout(createGrid());
        content.addClassName("grid-view");
        content.setBoxSizing(BoxSizing.BORDER_BOX);
        content.setHeightFull();
        content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        return content;
    }


    private Grid createGrid(){
        grid = new Grid();
        grid.setSizeFull();
        grid.setDataProvider(dataProvider);

//        grid.addSelectionListener(event -> event.getFirstSelectedItem()
//                .ifPresent(this::showDetails));

        grid.addColumn(BasicServiceProvider::getTypeService)
                .setFlexGrow(1)
                .setKey("typeService")
                .setHeader("Tipo Servicio")
                .setSortable(true)
                .setAutoWidth(true)
                .setResizable(true);
        grid.addColumn(BasicServiceProvider::getProvider)
                .setFlexGrow(1)
                .setKey("provider")
                .setHeader("Proveedor")
                .setSortable(true)
                .setAutoWidth(true)
                .setResizable(true);
        grid.addColumn(BasicServiceProvider::getDescription)
                .setFlexGrow(1)
                .setKey("description")
                .setHeader("Descripción Proveedor")
                .setSortable(true)
                .setAutoWidth(true)
                .setResizable(true);
        grid.addColumn(BasicServiceProvider::getNit)
                .setFlexGrow(1)
                .setKey("nit")
                .setHeader("NIT")
                .setSortable(true)
                .setAutoWidth(true)
                .setResizable(true);
        grid.addColumn(new ComponentRenderer<>(this::createButtonEdit))
                .setFlexGrow(0)
                .setAutoWidth(true);

        HeaderRow hr = grid.appendHeaderRow();

        typeServiceFilter = new ComboBox<>();
        typeServiceFilter.setItems(typeServiceList);
        typeServiceFilter.setWidthFull();
        typeServiceFilter.addValueChangeListener(event -> {
            applyFilter(dataProvider);
        });
        hr.getCell(grid.getColumnByKey("typeService")).setComponent(typeServiceFilter);

        providerFilter = new TextField();
        providerFilter.setValueChangeMode(ValueChangeMode.EAGER);
        providerFilter.setWidthFull();
        providerFilter.addValueChangeListener(e -> {
            applyFilter(dataProvider);
        });
        hr.getCell(grid.getColumnByKey("provider")).setComponent(providerFilter);

        descriptionFilter = new TextField();
        descriptionFilter.setValueChangeMode(ValueChangeMode.EAGER);
        descriptionFilter.setWidthFull();
        descriptionFilter.addValueChangeListener(e -> {
            applyFilter(dataProvider);
        });
        hr.getCell(grid.getColumnByKey("description")).setComponent(descriptionFilter);

        nitFilter = new IntegerField();
        nitFilter.setValueChangeMode(ValueChangeMode.EAGER);
        nitFilter.setWidthFull();
        nitFilter.setMin(0);
        nitFilter.addValueChangeListener(e -> {
            applyFilter(dataProvider);
        });
        hr.getCell(grid.getColumnByKey("nit")).setComponent(nitFilter);

        return grid;
    }

    private Component createButtonEdit(BasicServiceProvider basicServiceProvider){
        Button btn = new Button();
        Tooltips.getCurrent().setTooltip(btn,"Editar Registro");
        btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_SUCCESS);
        btn.setIcon(VaadinIcon.EDIT.create());
        btn.addClickListener(buttonClickEvent -> {
           showDetails(basicServiceProvider);
        });

        return btn;
    }

    private void showDetails(BasicServiceProvider basicServiceProvider){
        current = basicServiceProvider;
        detailsDrawerHeader.setTitle("Proveedor: ".concat(basicServiceProvider.getProvider()==null?"Nuevo":basicServiceProvider.getProvider()));
        detailsDrawer.setContent(createDetails(basicServiceProvider));
        detailsDrawer.show();
        binder.readBean(basicServiceProvider);
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
                BasicServiceProvider result = restTemplate.add(current);
                if (current.getId()==null){
                    basicServiceProviderList.add(result);
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

    private FormLayout createDetails(BasicServiceProvider baseServiceProvider){
        ComboBox<String> typeService = new ComboBox<>();
        typeService.setItems(typeServiceList);
        typeService.setWidthFull();
        typeService.setRequired(true);

        TextField provider = new TextField();
        provider.setWidthFull();
        provider.setRequired(true);

        TextField description = new TextField();
        description.setWidthFull();
        description.setRequired(true);

        IntegerField nit = new IntegerField();
        nit.setWidthFull();

        RadioButtonGroup<String> state = new RadioButtonGroup<>();
        state.setItems("ACTIVO","BAJA");
        state.setValue("ACTIVO");
        state.setRequired(true);

        binder = new BeanValidationBinder<>(BasicServiceProvider.class);
        binder.forField(typeService).asRequired("Tipo de Servicio es requerido")
                .bind(BasicServiceProvider::getTypeService,BasicServiceProvider::setTypeService);
        binder.forField(provider).asRequired("Proveedor es requerido")
                .bind(BasicServiceProvider::getProvider,BasicServiceProvider::setProvider);
        binder.forField(description).asRequired("Descripción del proveedor es requerida")
                .bind(BasicServiceProvider::getDescription,BasicServiceProvider::setDescription);
        binder.forField(nit)
                .bind(BasicServiceProvider::getNit,BasicServiceProvider::setNit);
        binder.forField(state).asRequired("Estado del proveedor es requerido")
                .bind(BasicServiceProvider::getState,BasicServiceProvider::setState);

        binder.addStatusChangeListener(event -> {
            boolean isValid = !event.hasValidationErrors();
            boolean hasChanges = binder.hasChanges();
            footer.saveState(hasChanges && isValid && GrantOptions.grantedOptionWrite("Proveedor Serv. Básicos"));
        });

        FormLayout form = new FormLayout();
        form.addClassNames(LumoStyles.Padding.Bottom.L,
                LumoStyles.Padding.Horizontal.S, LumoStyles.Padding.Top.S);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1,
                        FormLayout.ResponsiveStep.LabelsPosition.TOP),
                new FormLayout.ResponsiveStep("21em", 2,
                        FormLayout.ResponsiveStep.LabelsPosition.TOP));
        form.addFormItem(typeService,"Tipo de Servicio");
        form.addFormItem(provider,"Proveedor");
        FormLayout.FormItem descriptionItem = form.addFormItem(description,"Descripción");
        UIUtils.setColSpan(2,descriptionItem);
        form.addFormItem(nit,"NIT");
        form.addFormItem(state,"Estado");


        return form;
    }

    private void applyFilter(ListDataProvider<BasicServiceProvider> dataProvider){
        dataProvider.clearFilters();
        if(typeServiceFilter.getValue()!=null){
            dataProvider.addFilter(basicServiceProvider ->  Objects.equals(typeServiceFilter.getValue(),basicServiceProvider.getTypeService()));
        }
        if(!providerFilter.getValue().trim().equals("")){
            dataProvider.addFilter(basicServiceProvider -> StringUtils.containsIgnoreCase(basicServiceProvider.getProvider(),providerFilter.getValue()));
        }
        if(!descriptionFilter.getValue().trim().equals("")){
            dataProvider.addFilter(basicServiceProvider -> StringUtils.containsIgnoreCase(basicServiceProvider.getDescription(),descriptionFilter.getValue()));
        }
        if(nitFilter.getValue()!=null){
            dataProvider.addFilter(basicServiceProvider ->Objects.equals(basicServiceProvider.getNit(),nitFilter.getValue()));
        }

    }

    private void getBasicServiceProvider(){
        basicServiceProviderList = new ArrayList<>(restTemplate.getAll());
        typeServiceList = utilValues.getValueParameterByCategory("TIPO SERVICIO BASICO");
        dataProvider = new ListDataProvider<>(basicServiceProviderList);
    }
}
