package com.mindware.ui.views.basicServices;

import com.mindware.backend.entity.basicServices.BasicServicesDto;
import com.mindware.backend.rest.basicServices.BasicServicesDtoRestTemplate;
import com.mindware.ui.MainLayout;
import com.mindware.ui.components.FlexBoxLayout;
import com.mindware.ui.layout.size.Horizontal;
import com.mindware.ui.layout.size.Top;
import com.mindware.ui.util.css.BoxSizing;
import com.mindware.ui.views.ViewFrame;
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
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.*;
import dev.mett.vaadin.tooltip.Tooltips;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@Route(value = "basicservices", layout = MainLayout.class)
@ParentLayout(MainLayout.class)
@PageTitle("Servicios Basicos")
public class BasicServicesView extends ViewFrame implements RouterLayout {

    @Autowired
    private BasicServicesDtoRestTemplate restTemplate;

    private ListDataProvider<BasicServicesDto> dataProvider;

    private List<BasicServicesDto> basicServicesDtoList;

    private Button btnNew;

    private TextField nameBasicServiceProviderFilter;
    private TextField typeBasicServiceFilter;
    private TextField periodFilter;
    private TextField typeDocumentReceivedFilter;

    @Override
    protected void onAttach(AttachEvent attachEvent){
        super.onAttach(attachEvent);
        getListBasicServices();
        setViewHeader(createTopBar());
        setViewContent(createContent());
    }

    private HorizontalLayout createTopBar(){
        btnNew = new Button("Nuevo Pago Servicio Basico");
        btnNew.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnNew.setIcon(VaadinIcon.PLUS_CIRCLE.create());
        btnNew.addClickShortcut(Key.KEY_N, KeyModifier.ALT);
        btnNew.addClickListener(e -> {
            Map<String, List<String>> param = new HashMap<>();
            List<String> id = new ArrayList<>();
            id.add("NUEVO");

            param.put("id",id);

            QueryParameters qp = new QueryParameters(param);
            UI.getCurrent().navigate("basicservices-register",qp);
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
        FlexBoxLayout content = new FlexBoxLayout(createGridBasicServicesDto());
        content.addClassName("grid-view");
        content.setHeightFull();
        content.setBoxSizing(BoxSizing.BORDER_BOX);
        content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);

        return content;

    }

    private Grid createGridBasicServicesDto(){
        Grid<BasicServicesDto> grid = new Grid<>();
        grid.setMultiSort(true);
        grid.setSizeFull();
        grid.setDataProvider(dataProvider);

        grid.addColumn(BasicServicesDto::getNameBasicServiceProvider)
                .setFlexGrow(1)
                .setKey("nameBasicServiceProvider")
                .setHeader("Proveedor")
                .setSortable(true)
                .setAutoWidth(true)
                .setResizable(true);
        grid.addColumn(BasicServicesDto::getTypeBasicService)
                .setFlexGrow(1)
                .setKey("typeBasicService")
                .setHeader("Servicio")
                .setSortable(true)
                .setAutoWidth(true)
                .setResizable(true);
        grid.addColumn(BasicServicesDto::getPeriod)
                .setFlexGrow(1)
                .setKey("period")
                .setHeader("Periodo Pago")
                .setSortable(true)
                .setAutoWidth(true)
                .setResizable(true);
        grid.addColumn(BasicServicesDto::getTypeDocumentReceived)
                .setFlexGrow(1)
                .setKey("typeDocumentReceived")
                .setHeader("Tipo Documento")
                .setSortable(true)
                .setAutoWidth(true)
                .setResizable(true);
        grid.addColumn(BasicServicesDto::getNumberDocumentReceived)
                .setFlexGrow(1)
                .setHeader("Nro Factura/Recibo")
                .setSortable(true)
                .setAutoWidth(true)
                .setResizable(true);
        grid.addColumn(new NumberRenderer<>(BasicServicesDto::getAmount,
                        " %(,.2f",
                        Locale.US, "0.00") )
                .setFlexGrow(1)
                .setHeader("Monto (Bs)")
                .setSortable(true)
                .setAutoWidth(true)
                .setResizable(true);
        grid.addColumn(new ComponentRenderer<>(this::createButtonEdit))
                .setFlexGrow(0)
                .setAutoWidth(true);

        HeaderRow hr = grid.appendHeaderRow();

        nameBasicServiceProviderFilter = new TextField();
        nameBasicServiceProviderFilter.setValueChangeMode(ValueChangeMode.EAGER);
        nameBasicServiceProviderFilter.setWidthFull();
        nameBasicServiceProviderFilter.addValueChangeListener(e -> {
            applyFilter(dataProvider);
        });
        hr.getCell(grid.getColumnByKey("nameBasicServiceProvider")).setComponent(nameBasicServiceProviderFilter);

        typeBasicServiceFilter = new TextField();
        typeBasicServiceFilter.setValueChangeMode(ValueChangeMode.EAGER);
        typeBasicServiceFilter.setWidthFull();
        typeBasicServiceFilter.addValueChangeListener(e -> {
            applyFilter(dataProvider);
        });
        hr.getCell(grid.getColumnByKey("typeBasicService")).setComponent(typeBasicServiceFilter);

        periodFilter = new TextField();
        periodFilter.setValueChangeMode(ValueChangeMode.EAGER);
        periodFilter.setWidthFull();
        periodFilter.addValueChangeListener(e -> {
            applyFilter(dataProvider);
        });
        hr.getCell(grid.getColumnByKey("period")).setComponent(periodFilter);

        typeDocumentReceivedFilter = new TextField();
        typeDocumentReceivedFilter.setValueChangeMode(ValueChangeMode.EAGER);
        typeDocumentReceivedFilter.setWidthFull();
        typeDocumentReceivedFilter.addValueChangeListener(e -> {
            applyFilter(dataProvider);
        });
        hr.getCell(grid.getColumnByKey("typeDocumentReceived")).setComponent(typeDocumentReceivedFilter);

        return grid;
    }

    private Component createButtonEdit(BasicServicesDto basicServicesDto){
        Button btn = new Button();
        Tooltips.getCurrent().setTooltip(btn,"Editar Registro");
        btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_SUCCESS);
        btn.setIcon(VaadinIcon.EDIT.create());
        btn.addClickListener(event -> {
            Map<String, List<String>> param = new HashMap<>();
            List<String> id = new ArrayList<>();
            id.add(basicServicesDto.getId().toString());
            param.put("id",id);

            QueryParameters qp = new QueryParameters(param);
            UI.getCurrent().navigate("basicservices-register",qp);
        });
        return btn;
    }


    private void applyFilter(ListDataProvider<BasicServicesDto> dataProvider){
        dataProvider.clearFilters();
        if(!nameBasicServiceProviderFilter.getValue().trim().equals("")){
            dataProvider.addFilter(basicServicesDto ->
                    StringUtils.containsIgnoreCase(basicServicesDto.getNameBasicServiceProvider(),
                            nameBasicServiceProviderFilter.getValue().trim()));
        }
        if(!typeBasicServiceFilter.getValue().trim().equals("")){
            dataProvider.addFilter(basicServicesDto ->
                    StringUtils.containsIgnoreCase(basicServicesDto.getTypeBasicService(),
                            typeBasicServiceFilter.getValue().trim()));
        }
        if(!periodFilter.getValue().trim().equals("")){
            dataProvider.addFilter(basicServicesDto ->
                    StringUtils.containsIgnoreCase(basicServicesDto.getPeriod(),
                            periodFilter.getValue().trim()));
        }
        if(!typeDocumentReceivedFilter.getValue().trim().equals("")){
            dataProvider.addFilter(basicServicesDto ->
                    StringUtils.containsIgnoreCase(basicServicesDto.getTypeDocumentReceived(),
                            typeDocumentReceivedFilter.getValue().trim()));
        }

    }

    private void getListBasicServices(){
        basicServicesDtoList = new ArrayList<>(restTemplate.getAll());
        dataProvider = new ListDataProvider<>(basicServicesDtoList);
    }
}