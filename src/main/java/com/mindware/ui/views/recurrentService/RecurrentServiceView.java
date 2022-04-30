package com.mindware.ui.views.recurrentService;

import com.mindware.backend.entity.contract.Contract;
import com.mindware.backend.entity.recurrentService.RecurrentServiceDto;
import com.mindware.backend.rest.recurrentService.RecurrentServiceDtoRestTemplate;
import com.mindware.ui.MainLayout;
import com.mindware.ui.components.FlexBoxLayout;
import com.mindware.ui.layout.size.Horizontal;
import com.mindware.ui.layout.size.Top;
import com.mindware.ui.util.css.BoxSizing;
import com.mindware.ui.views.ViewFrame;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.ColumnTextAlign;
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

@Route(value = "recurrent-service", layout = MainLayout.class)
@ParentLayout(MainLayout.class)
@PageTitle("Servicios Recurrentes")
public class RecurrentServiceView extends ViewFrame implements RouterLayout {

    @Autowired
    RecurrentServiceDtoRestTemplate restTemplate;

    private ListDataProvider<RecurrentServiceDto> dataProvider;

    private List<RecurrentServiceDto> recurrentServiceDtoList = new ArrayList<>();

    private Button btnNew;

    private TextField supplierNameFilter;
    private TextField supplierLocationFilter;
    private TextField periodFilter;
    private TextField paymentFrecuencyFilter;

    @Override
    protected void onAttach(AttachEvent attachEvent){
        super.onAttach(attachEvent);
        getListRecurrentService();
        setViewHeader(createTopBar());
        setViewContent(createContent());
    }

    private HorizontalLayout createTopBar(){
        btnNew = new Button("Nuevo Pago Servicio Recurrente");
        btnNew.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnNew.setIcon(VaadinIcon.PLUS_CIRCLE.create());
        btnNew.addClickShortcut(Key.KEY_N, KeyModifier.ALT);
        btnNew.addClickListener(e -> {
            Map<String, List<String>> param = new HashMap<>();
            List<String> id = new ArrayList<>();
            id.add("NUEVO");

            param.put("id",id);

            QueryParameters qp = new QueryParameters(param);
            UI.getCurrent().navigate("recurrent-service-register",qp);
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
        FlexBoxLayout content = new FlexBoxLayout(createGridRecurrentServiceDto());
        content.addClassName("grid-view");
        content.setHeightFull();
        content.setBoxSizing(BoxSizing.BORDER_BOX);
        content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);

        return content;

    }

    private Grid createGridRecurrentServiceDto(){
        Grid<RecurrentServiceDto> grid = new Grid();
        grid.setMultiSort(true);
        grid.setSizeFull();
        grid.setDataProvider(dataProvider);

        grid.addColumn(RecurrentServiceDto::getSupplierName)
                .setFlexGrow(1)
                .setKey("supplierName")
                .setHeader("Proveedor")
                .setSortable(true)
                .setAutoWidth(true)
                .setResizable(true);
        grid.addColumn(RecurrentServiceDto::getSupplierLocation)
                .setFlexGrow(1)
                .setKey("supplierLocation")
                .setHeader("Ubicacion Proveedor")
                .setSortable(true)
                .setAutoWidth(true)
                .setResizable(true);
        grid.addColumn(RecurrentServiceDto::getPeriod)
                .setFlexGrow(1)
                .setKey("period")
                .setHeader("Periodo")
                .setSortable(true)
                .setAutoWidth(true)
                .setResizable(true);
        grid.addColumn(RecurrentServiceDto::getPaymentFrecuency)
                .setFlexGrow(1)
                .setKey("paymentFrecuency")
                .setHeader("Frecuencia Pago")
                .setSortable(true)
                .setAutoWidth(true)
                .setResizable(true);
//        grid.addColumn(RecurrentServiceDto::getCurrency)
//                .setFlexGrow(1)
//                .setHeader("Moneda")
//                .setSortable(true)
//                .setAutoWidth(true)
//                .setResizable(true);
        grid.addColumn(new NumberRenderer<>(RecurrentServiceDto::getAmount,
                        " %(,.2f",
                        Locale.US, "0.00") )
                .setFlexGrow(1)
                .setHeader("Monto (Bs)")
                .setSortable(true)
                .setAutoWidth(true)
                .setResizable(true);
        grid.addColumn(new ComponentRenderer<>(this::createButtonEdit))
                .setFlexGrow(1)
                .setTextAlign(ColumnTextAlign.START);

        HeaderRow hr = grid.appendHeaderRow();

        supplierNameFilter = new TextField();
        supplierNameFilter.setValueChangeMode(ValueChangeMode.EAGER);
        supplierNameFilter.setWidthFull();
        supplierNameFilter.addValueChangeListener(e -> applyFilter(dataProvider));
        hr.getCell(grid.getColumnByKey("supplierName")).setComponent(supplierNameFilter);

        supplierLocationFilter = new TextField();
        supplierLocationFilter.setValueChangeMode(ValueChangeMode.EAGER);
        supplierLocationFilter.setWidthFull();
        supplierLocationFilter.addValueChangeListener(e -> applyFilter(dataProvider));
        hr.getCell(grid.getColumnByKey("supplierLocation")).setComponent(supplierLocationFilter);

        periodFilter = new TextField();
        periodFilter.setValueChangeMode(ValueChangeMode.EAGER);
        periodFilter.setWidthFull();
        periodFilter.addValueChangeListener(e -> applyFilter(dataProvider));
        hr.getCell(grid.getColumnByKey("period")).setComponent(periodFilter);

        paymentFrecuencyFilter = new TextField();
        paymentFrecuencyFilter.setValueChangeMode(ValueChangeMode.EAGER);
        paymentFrecuencyFilter.setWidthFull();
        paymentFrecuencyFilter.addValueChangeListener(e -> applyFilter(dataProvider));
        hr.getCell(grid.getColumnByKey("paymentFrecuency")).setComponent(paymentFrecuencyFilter);

        return grid;
    }

    private Component createButtonEdit(RecurrentServiceDto recurrentServiceDto){
        Button btn = new Button();
        Tooltips.getCurrent().setTooltip(btn,"Editar Registro");
        btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_SUCCESS);
        btn.setIcon(VaadinIcon.EDIT.create());
        btn.addClickListener(buttonClickEvent -> {
            Map<String, List<String>> param = new HashMap<>();
            List<String> id = new ArrayList<>();
            id.add(recurrentServiceDto.getId().toString());

            param.put("id",id);

            QueryParameters qp = new QueryParameters(param);
            UI.getCurrent().navigate("recurrent-service-register",qp);
        });

        return btn;
    }

    private void applyFilter(ListDataProvider<RecurrentServiceDto> dataProvider){
        dataProvider.clearFilters();
        if(!supplierNameFilter.getValue().trim().equals("")){
            dataProvider.addFilter(recurrentServiceDto -> StringUtils.containsIgnoreCase(
                    recurrentServiceDto.getSupplierName(),supplierNameFilter.getValue()));
        }
        if(!supplierLocationFilter.getValue().trim().equals("")){
            dataProvider.addFilter(recurrentServiceDto -> StringUtils.containsIgnoreCase(
                    recurrentServiceDto.getSupplierLocation(),supplierLocationFilter.getValue()));
        }
        if(!periodFilter.getValue().trim().equals("")){
            dataProvider.addFilter(recurrentServiceDto -> StringUtils.containsIgnoreCase(
                    recurrentServiceDto.getPeriod(),periodFilter.getValue()));
        }
        if(!paymentFrecuencyFilter.getValue().trim().equals("")){
            dataProvider.addFilter(recurrentServiceDto -> StringUtils.containsIgnoreCase(
                    recurrentServiceDto.getPaymentFrecuency(),paymentFrecuencyFilter.getValue()));
        }
    }

    private void getListRecurrentService(){
        recurrentServiceDtoList = new ArrayList<>(restTemplate.getAll());
        dataProvider = new ListDataProvider<>(recurrentServiceDtoList);
    }
}
