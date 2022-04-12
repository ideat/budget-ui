package com.mindware.ui.views.supplier;

import com.mindware.backend.entity.supplier.Supplier;
import com.mindware.backend.rest.supplier.SupplierRestTemplate;
import com.mindware.ui.MainLayout;
import com.mindware.ui.components.FlexBoxLayout;
import com.mindware.ui.components.detailsdrawer.DetailsDrawer;
import com.mindware.ui.components.detailsdrawer.DetailsDrawerHeader;
import com.mindware.ui.layout.size.Horizontal;
import com.mindware.ui.layout.size.Top;
import com.mindware.ui.util.css.BoxSizing;
import com.mindware.ui.views.SplitViewFrame;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@Route(value = "supplier", layout = MainLayout.class)
@ParentLayout(MainLayout.class)
@PageTitle("Proveedores")
public class SupplierView extends SplitViewFrame implements RouterLayout {

    @Autowired
    SupplierRestTemplate restTemplate;

    private List<Supplier> supplierList = new ArrayList<>();

    private Button btnNew;

    private ListDataProvider<Supplier> dataProvider;

    private TextField nameFilter;
    private TextField nitFilter;
    private TextField locationFilter;
    private TextField areaWorkFilter;
    private TextField primaryActivityFilter;

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        getListSupplier();
        setViewHeader(createTopBar());
        setViewContent(createContent());

    }

    private HorizontalLayout createTopBar() {
        btnNew = new Button("Nuevo Proveedor");
        btnNew.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnNew.setIcon(VaadinIcon.PLUS_CIRCLE.create());
        btnNew.addClickShortcut(Key.KEY_N, KeyModifier.ALT);
        btnNew.addClickListener(e -> {
            UI.getCurrent().navigate(SupplierRegisterView.class,"NUEVO");
        });

        HorizontalLayout topLayout = new HorizontalLayout();
        topLayout.setWidth("100%");
        topLayout.add(btnNew);
        topLayout.setVerticalComponentAlignment(FlexComponent.Alignment.END, btnNew);
        topLayout.setSpacing(true);
        topLayout.setPadding(true);

        return topLayout;
    }

    private Component createContent() {
        FlexBoxLayout content = new FlexBoxLayout(createGridSupplier());
        content.addClassName("grid-view");
        content.setHeightFull();
        content.setBoxSizing(BoxSizing.BORDER_BOX);
        content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);

        return content;

    }

    private Grid createGridSupplier(){
        Grid<Supplier> grid = new Grid<>();
        grid.setMultiSort(true);
        grid.setSizeFull();
        grid.setDataProvider(dataProvider);
        grid.addSelectionListener(event -> {
            UI.getCurrent().navigate(SupplierRegisterView.class,event.getFirstSelectedItem().get().getId().toString());
        });

        grid.addColumn(Supplier::getName)
                .setFlexGrow(1)
                .setKey("name")
                .setHeader("Proveedor")
                .setSortable(true)
                .setAutoWidth(true)
                .setResizable(true);
        grid.addColumn(Supplier::getNit)
                .setFlexGrow(1)
                .setKey("nit")
                .setHeader("NIT")
                .setSortable(true)
                .setAutoWidth(true)
                .setResizable(true);
        grid.addColumn(Supplier::getLocation)
                .setFlexGrow(1)
                .setKey("location")
                .setHeader("Oficina")
                .setSortable(true)
                .setAutoWidth(true)
                .setResizable(true);
        grid.addColumn(Supplier::getAreaWork)
                .setFlexGrow(1)
                .setKey("areaWork")
                .setHeader("Rubro")
                .setSortable(true)
                .setAutoWidth(true)
                .setResizable(true);
        grid.addColumn(Supplier::getPrimaryActivity)
                .setFlexGrow(1)
                .setKey("primaryActivity")
                .setHeader("Actividad Principal")
                .setSortable(true)
                .setAutoWidth(true)
                .setResizable(true);

        HeaderRow hr = grid.appendHeaderRow();

        nameFilter = new TextField();
        nameFilter.setValueChangeMode(ValueChangeMode.EAGER);
        nameFilter.setWidthFull();
        nameFilter.addValueChangeListener(e ->{
            appplyFilter(dataProvider);
        });
        hr.getCell(grid.getColumnByKey("name")).setComponent(nameFilter);

        nitFilter = new TextField();
        nitFilter.setValueChangeMode(ValueChangeMode.EAGER);
        nitFilter.setWidthFull();
        nitFilter.addValueChangeListener(e -> appplyFilter(dataProvider));
        hr.getCell(grid.getColumnByKey("nit")).setComponent(nitFilter);

        locationFilter = new TextField();
        locationFilter.setValueChangeMode(ValueChangeMode.EAGER);
        locationFilter.setWidthFull();
        locationFilter.addValueChangeListener(e -> appplyFilter(dataProvider));
        hr.getCell(grid.getColumnByKey("location")).setComponent(locationFilter);

        areaWorkFilter = new TextField();
        areaWorkFilter.setValueChangeMode(ValueChangeMode.EAGER);
        areaWorkFilter.setWidthFull();
        areaWorkFilter.addValueChangeListener(e -> appplyFilter(dataProvider));
        hr.getCell(grid.getColumnByKey("areaWork")).setComponent(areaWorkFilter);

        primaryActivityFilter = new TextField();
        primaryActivityFilter.setValueChangeMode(ValueChangeMode.EAGER);
        primaryActivityFilter.setWidthFull();
        primaryActivityFilter.addValueChangeListener(e -> appplyFilter(dataProvider));
        hr.getCell(grid.getColumnByKey("primaryActivity")).setComponent(primaryActivityFilter);

        return grid;
    }


    private void appplyFilter(ListDataProvider<Supplier> dataProvider){
        dataProvider.clearFilters();
        if(!nameFilter.getValue().trim().equals("")){
            dataProvider.addFilter(supplier -> StringUtils.containsIgnoreCase(supplier.getName(), nameFilter.getValue()));
        }
        if(!nitFilter.getValue().trim().equals("")){
            dataProvider.addFilter(supplier -> StringUtils.containsIgnoreCase(supplier.getNit(), nitFilter.getValue()));
        }
        if(!locationFilter.getValue().trim().equals("")){
            dataProvider.addFilter(supplier -> StringUtils.containsIgnoreCase(supplier.getLocation(), locationFilter.getValue()));
        }
        if(!areaWorkFilter.getValue().trim().equals("")){
            dataProvider.addFilter(supplier -> StringUtils.containsIgnoreCase(supplier.getAreaWork(), areaWorkFilter.getValue()));
        }
        if(!primaryActivityFilter.getValue().trim().equals("")){
            dataProvider.addFilter(supplier -> StringUtils.containsIgnoreCase(supplier.getPrimaryActivity(), primaryActivityFilter.getValue()));
        }
    }

    private void getListSupplier(){
        supplierList = new ArrayList<>(restTemplate.getAll());
        dataProvider = new ListDataProvider<>(supplierList);
    }
}