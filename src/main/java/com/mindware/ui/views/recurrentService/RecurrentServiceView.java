package com.mindware.ui.views.recurrentService;

import com.mindware.backend.entity.contract.Contract;
import com.mindware.backend.entity.recurrentService.RecurrentService;
import com.mindware.backend.entity.recurrentService.RecurrentServiceDto;
import com.mindware.backend.rest.recurrentService.RecurrentServiceDtoRestTemplate;
import com.mindware.backend.rest.recurrentService.RecurrentServiceRestTemplate;
import com.mindware.backend.util.GrantOptions;
import com.mindware.ui.MainLayout;
import com.mindware.ui.components.FlexBoxLayout;
import com.mindware.ui.layout.size.Horizontal;
import com.mindware.ui.layout.size.Top;
import com.mindware.ui.util.UIUtils;
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
import com.vaadin.flow.server.VaadinSession;
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

    @Autowired
    private RecurrentServiceRestTemplate recurrentServiceRestTemplate;

    private ListDataProvider<RecurrentServiceDto> dataProvider;

    private List<RecurrentServiceDto> recurrentServiceDtoList = new ArrayList<>();

    private Button btnNew;

    private TextField supplierNameFilter;
    private TextField supplierLocationFilter;
    private TextField periodFilter;
    private TextField paymentFrecuencyFilter;
    private TextField stateFilter;

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
        btnNew.setVisible(GrantOptions.grantedOptionWrite("Servicios Recurrentes"));
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
                .setHeader("Ubicación Proveedor")
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
        grid.addColumn(RecurrentServiceDto::getState)
                .setFlexGrow(1)
                .setKey("state")
                .setHeader("Estado")
                .setSortable(true)
                .setAutoWidth(true)
                .setResizable(true);
        grid.addColumn(new ComponentRenderer<>(this::createButtonEdit))
                .setFlexGrow(0)
                .setTextAlign(ColumnTextAlign.START);
        grid.addColumn(new ComponentRenderer<>(this::createButtonSend))
                .setFlexGrow(0)
                .setTextAlign(ColumnTextAlign.START);
        grid.addColumn(new ComponentRenderer<>(this::createButtonRegard))
                .setFlexGrow(0)
                .setTextAlign(ColumnTextAlign.START);
        grid.addColumn(new ComponentRenderer<>(this::createButtonFinish))
                .setFlexGrow(0)
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

        stateFilter = new TextField();
        stateFilter.setValueChangeMode(ValueChangeMode.EAGER);
        stateFilter.setWidthFull();
        stateFilter.addValueChangeListener(e -> {
            applyFilter(dataProvider);
        });
        hr.getCell(grid.getColumnByKey("state")).setComponent(stateFilter);

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

    private Component createButtonSend(RecurrentServiceDto recurrentServiceDto){
        Button btn = new Button();
        Tooltips.getCurrent().setTooltip(btn,"Enviar");
        btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btn.setIcon(VaadinIcon.THUMBS_UP.create());
        btn.setVisible(GrantOptions.grantedOptionSend("Servicios Recurrentes"));
        btn.addClickListener(event -> {
            if(recurrentServiceDto.getInvoiceAuthorizer()==null || (recurrentServiceDto.getInvoiceAuthorizer().equals("[]"))){
                UIUtils.showNotificationType("Registre Autorizador de la factura","alert");
                return;
            }
            RecurrentService recurrentService = new RecurrentService();
            recurrentService.setId(recurrentServiceDto.getId());
            recurrentService.setState("ENVIADO");
            recurrentServiceRestTemplate.updateState(recurrentService);
            recurrentServiceDtoList.remove(recurrentServiceDto);
            if(VaadinSession.getCurrent().getAttribute("scope").toString().equals("NACIONAL")){
                recurrentServiceDto.setState("ENVIADO");
                recurrentServiceDtoList.add(recurrentServiceDto);
//                dataProvider.refreshItem(recurrentServiceDto);
            }
            dataProvider.refreshAll();
            UIUtils.showNotificationType("Enviado a Oficina Nacional","success");
        });
        return btn;
    }

    private Component createButtonRegard(RecurrentServiceDto recurrentServiceDto){
        Button btn = new Button();
        Tooltips.getCurrent().setTooltip(btn,"Observado");
        btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        btn.setIcon(VaadinIcon.THUMBS_DOWN_O.create());
        btn.setVisible(GrantOptions.grantedOptionObserved("Servicios Recurrentes"));
        btn.addClickListener(event -> {
            if(!recurrentServiceDto.getState().equals("ENVIADO") /*|| !recurrentServiceDto.getState().equals("FINALIZADO")*/ ){
                UIUtils.showNotificationType("No puede OBSERVARSE antes de ser ENVIADO","alert");
                return;
            }
            RecurrentService recurrentService = new RecurrentService();
            recurrentService.setId(recurrentServiceDto.getId());
            recurrentService.setState("OBSERVADO");
            recurrentServiceDtoList.remove(recurrentServiceDto);
            recurrentServiceDto.setState("OBSERVADO");
            recurrentServiceDtoList.add(recurrentServiceDto);
            dataProvider.refreshItem(recurrentServiceDto);
            recurrentServiceRestTemplate.updateState(recurrentService);
            UIUtils.showNotificationType("Servicio Observado","success");
        });
        return btn;
    }

    private Component createButtonFinish(RecurrentServiceDto recurrentServiceDto){
        Button btn = new Button();
        Tooltips.getCurrent().setTooltip(btn,"Finalizar");
        btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_CONTRAST);
        btn.setIcon(VaadinIcon.LOCK.create());
        btn.setVisible(GrantOptions.grantedOptionFinish("Servicios Recurrentes"));
        btn.addClickListener(event -> {
            if(!recurrentServiceDto.getState().equals("ENVIADO")){
                UIUtils.showNotificationType("No puede finalizar sin estar ENVIADO","alert");
                return;
            }
            if(recurrentServiceDto.getDateDeliveryAccounting()==null){
                UIUtils.showNotificationType("No puede finalizar sin enviar a Contabilidad","alert");
                return;
            }
            RecurrentService recurrentService = new RecurrentService();
            recurrentService.setId(recurrentServiceDto.getId());
            recurrentService.setState("FINALIZADO");
            recurrentServiceDtoList.remove(recurrentServiceDto);
            recurrentServiceDto.setState("FINALIZADO");
            recurrentServiceDtoList.add(recurrentServiceDto);
            dataProvider.refreshItem(recurrentServiceDto);
            recurrentServiceRestTemplate.updateState(recurrentService);
            UIUtils.showNotificationType("Servicio Finalizado","success");
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
        if(!stateFilter.getValue().trim().equals("")){
            dataProvider.addFilter(recurrentServiceDto -> StringUtils.containsIgnoreCase(
                    recurrentServiceDto.getState(),stateFilter.getValue()));
        }
    }

    private void getListRecurrentService(){
        if(VaadinSession.getCurrent().getAttribute("scope").toString().equals("NACIONAL")) {
            recurrentServiceDtoList = new ArrayList<>(restTemplate.getAll());
        }else{
            recurrentServiceDtoList = new ArrayList<>(restTemplate.getByCreatedByAndState(VaadinSession.getCurrent().getAttribute("login").toString()));
        }

        dataProvider = new ListDataProvider<>(recurrentServiceDtoList);
    }
}
