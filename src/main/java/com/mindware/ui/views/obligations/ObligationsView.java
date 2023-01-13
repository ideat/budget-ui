package com.mindware.ui.views.obligations;

import com.mindware.backend.entity.obligations.Obligations;
import com.mindware.backend.entity.obligations.ObligationsDto;
import com.mindware.backend.rest.obligations.ObligationsDtoRestTemplate;
import com.mindware.backend.rest.obligations.ObligationsRestTemplate;
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
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
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

@Route(value = "obligations", layout = MainLayout.class)
@ParentLayout(MainLayout.class)
@PageTitle("Obligaciones")
public class ObligationsView   extends ViewFrame implements RouterLayout {

    @Autowired
    private ObligationsDtoRestTemplate restTemplate;

    @Autowired
    private ObligationsRestTemplate obligationsRestTemplate;

    private ListDataProvider<ObligationsDto> dataProvider;

    private List<ObligationsDto> obligationsDtoList;

    private Button  btnNew;

    private TextField nameSupplierFilter;
    private TextField typeObligationFilter;
    private TextField periodFilter;
    private TextField typeDocumentReceivedFilter;
    private TextField stateFilter;

    @Override
    protected void onAttach(AttachEvent attachEvent){
        super.onAttach(attachEvent);
        getListObligations();
        setViewHeader(createTopBar());
        setViewContent(createContent());
    }

    private HorizontalLayout createTopBar(){
        btnNew = new Button("Nuevo Pago Obligación");
        btnNew.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnNew.setIcon(VaadinIcon.PLUS_CIRCLE.create());
        btnNew.addClickShortcut(Key.KEY_N, KeyModifier.ALT);
        btnNew.setVisible(GrantOptions.grantedOptionWrite("Obligaciones"));
        btnNew.addClickListener(e -> {
            Map<String, List<String>> param = new HashMap<>();
            List<String> id = new ArrayList<>();
            id.add("NUEVO");

            param.put("id",id);

            QueryParameters qp = new QueryParameters(param);
            UI.getCurrent().navigate("obligations-register",qp);
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
        FlexBoxLayout content = new FlexBoxLayout(createGridObligationsDto());
        content.addClassName("grid-view");
        content.setHeightFull();
        content.setBoxSizing(BoxSizing.BORDER_BOX);
        content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);

        return content;

    }

    private Grid createGridObligationsDto(){
        Grid<ObligationsDto> grid = new Grid<>();
        grid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_WRAP_CELL_CONTENT);
        grid.setMultiSort(true);
        grid.setSizeFull();
        grid.setDataProvider(dataProvider);

        grid.addColumn(ObligationsDto::getNameSupplier)
                .setFlexGrow(1)
                .setKey("nameSupplier")
                .setHeader("Proveedor")
                .setSortable(true)
//                .setAutoWidth(true)
                .setResizable(true);
        grid.addColumn(ObligationsDto::getTypeObligation)
                .setFlexGrow(1)
                .setKey("typeObligation")
                .setHeader("Tipo Obligación")
                .setSortable(true)
//                .setAutoWidth(true)
                .setResizable(true);

        grid.addColumn(ObligationsDto::getPeriod)
                .setFlexGrow(0)
                .setKey("period")
                .setHeader("Periodo Pago")
                .setSortable(true)
                .setAutoWidth(true)
                .setResizable(true);
        grid.addColumn(ObligationsDto::getTypeDocumentReceived)
                .setFlexGrow(0)
                .setKey("typeDocumentReceived")
                .setHeader("Tipo Documento")
                .setSortable(true)
                .setAutoWidth(true)
                .setResizable(true);
//        grid.addColumn(ObligationsDto::getNumberDocumentReceived)
//                .setFlexGrow(1)
//                .setHeader("Nro. Factura/CAABS")
//                .setSortable(true)
//                .setAutoWidth(true)
//                .setResizable(true);
        grid.addColumn(new NumberRenderer<>(ObligationsDto::getAmount,
                " %(,.2f",
                Locale.US, "0.00") )
                .setFlexGrow(0)
                .setHeader("Monto (Bs)")
                .setSortable(true)
                .setAutoWidth(true)
                .setResizable(true);
        grid.addColumn(ObligationsDto::getState)
                .setFlexGrow(0)
                .setKey("state")
                .setHeader("Estado")
                .setSortable(true)
                .setAutoWidth(true)
                .setResizable(true);
        grid.addColumn(new ComponentRenderer<>(this::createButtonEdit))
                .setFlexGrow(0)
                .setAutoWidth(true);
        grid.addColumn(new ComponentRenderer<>(this::createButtonSend))
                .setFlexGrow(0)
                .setAutoWidth(true);
        grid.addColumn(new ComponentRenderer<>(this::createButtonRegard))
                .setFlexGrow(0)
                .setAutoWidth(true);
        grid.addColumn(new ComponentRenderer<>(this::createButtonFinish))
                .setFlexGrow(0)
                .setAutoWidth(true);

        HeaderRow hr = grid.appendHeaderRow();

        nameSupplierFilter = new TextField();
        nameSupplierFilter.setValueChangeMode(ValueChangeMode.EAGER);
        nameSupplierFilter.setWidthFull();
        nameSupplierFilter.addValueChangeListener(e -> {
            applyFilter(dataProvider);
        });
        hr.getCell(grid.getColumnByKey("nameSupplier")).setComponent(nameSupplierFilter);

        typeObligationFilter = new TextField();
        typeObligationFilter.setValueChangeMode(ValueChangeMode.EAGER);
        typeObligationFilter.setWidthFull();
        typeObligationFilter.addValueChangeListener(e -> {
            applyFilter(dataProvider);
        });
        hr.getCell(grid.getColumnByKey("typeObligation")).setComponent(typeObligationFilter);


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

        stateFilter = new TextField();
        stateFilter.setValueChangeMode(ValueChangeMode.EAGER);
        stateFilter.setWidthFull();
        stateFilter.addValueChangeListener(e -> {
            applyFilter(dataProvider);
        });
        hr.getCell(grid.getColumnByKey("state")).setComponent(stateFilter);

        return grid;
    }

    private Component createButtonEdit(ObligationsDto obligationsDto){
        Button btn = new Button();
        Tooltips.getCurrent().setTooltip(btn,"Editar Registro");
        btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_SUCCESS);
        btn.setIcon(VaadinIcon.EDIT.create());
        btn.addClickListener(event -> {
            Map<String, List<String>> param = new HashMap<>();
            List<String> id = new ArrayList<>();
            id.add(obligationsDto.getId().toString());
            param.put("id",id);

            QueryParameters qp = new QueryParameters(param);
            UI.getCurrent().navigate("obligations-register",qp);
        });
        return btn;
    }

    private Component createButtonSend(ObligationsDto obligationsDto){
        Button btn = new Button();
        Tooltips.getCurrent().setTooltip(btn,"Enviar");
        btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btn.setIcon(VaadinIcon.THUMBS_UP.create());
        btn.setVisible(GrantOptions.grantedOptionSend("Obligaciones"));
        btn.addClickListener(event -> {
            if(obligationsDto.getInvoiceAuthorizer()==null || obligationsDto.getInvoiceAuthorizer().equals("[]")){
                UIUtils.showNotificationType("Registre Autorizador de la factura","alert");
                return;
            }
            if(obligationsDto.getState().equals("FINALIZADO")){
                UIUtils.showNotificationType("Operación ya fue Finalizada","alert");
                return;
            }
            if( obligationsDto.getInvoiceAuthorizer()==null || obligationsDto.getInvoiceAuthorizer().equals("[]")){
                UIUtils.showNotificationType("Registre autorizadores de la factura antes de Enviar","alert");
                return;
            }
            Obligations obligations = new Obligations();
            obligations.setId(obligationsDto.getId());
            obligations.setState("ENVIADO");
            obligationsRestTemplate.updateSate(obligations);
            obligationsDtoList.remove(obligationsDto);
            if(VaadinSession.getCurrent().getAttribute("scope").toString().equals("NACIONAL")){
                obligationsDto.setState("ENVIADO");
                obligationsDtoList.add(obligationsDto);
//                dataProvider.refreshItem(obligationsDto);
            }

            obligationsDtoList.sort(Comparator.comparing(ObligationsDto::getPaymentDate));
            dataProvider.refreshAll();
            UIUtils.showNotificationType("Enviado a Oficina Nacional","success");
        });
        return btn;
    }

    private Component createButtonRegard(ObligationsDto obligationsDto){
        Button btn = new Button();
        Tooltips.getCurrent().setTooltip(btn,"Observar");
        btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        btn.setIcon(VaadinIcon.THUMBS_DOWN_O.create());
        btn.setVisible(GrantOptions.grantedOptionObserved("Obligaciones"));
        btn.addClickListener(event -> {

            if(obligationsDto.getState().equals("FINALIZADO")){
                UIUtils.showNotificationType("Operación ya fue Finalizada","alert");
                return;
            }

            if(!obligationsDto.getState().equals("ENVIADO") ){
                UIUtils.showNotificationType("No puede OBSERVARSE antes de ser ENVIADA","alert");
                return;
            }

            Obligations obligations = new Obligations();
            obligations.setId(obligationsDto.getId());
            obligations.setState("OBSERVADO");
            obligationsRestTemplate.updateSate(obligations);
            obligationsDto.setState("OBSERVADO");
            obligationsDtoList.removeIf(ob -> ob.getId().equals(obligationsDto.getId()));
            obligationsDtoList.add(obligationsDto);
            obligationsDtoList.sort(Comparator.comparing(ObligationsDto::getPaymentDate));
            dataProvider.refreshAll();
            UIUtils.showNotificationType("Obligación Observada","success");
        });
        return btn;
    }

    private Component createButtonFinish(ObligationsDto obligationsDto){
        Button btn = new Button();
        Tooltips.getCurrent().setTooltip(btn,"Finalizar");
        btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_CONTRAST);
        btn.setIcon(VaadinIcon.LOCK.create());
        btn.setVisible(GrantOptions.grantedOptionFinish("Obligaciones"));
        btn.addClickListener(event -> {
            if(!obligationsDto.getState().equals("ENVIADO")){
                UIUtils.showNotificationType("No puede finalizar sin estar ENVIADO","alert");
                return;
            }
            if(obligationsDto.getDateDeliveryAccounting()==null){
                UIUtils.showNotificationType("No puede finalizar sin enviar a Contabilidad","alert");
                return;
            }
            if(obligationsDto.getState().equals("FINALIZADO")){
                UIUtils.showNotificationType("Operación ya fue Finalizada","alert");
                return;
            }
            Obligations obligations = new Obligations();
            obligations.setId(obligationsDto.getId());
            obligations.setState("FINALIZADO");
            obligationsDto.setState("FINALIZADO");
            obligationsRestTemplate.updateSate(obligations);
            obligationsDtoList.removeIf(ob -> ob.getId().equals(obligationsDto.getId()));
            obligationsDtoList.add(obligationsDto);
            obligationsDtoList.sort(Comparator.comparing(ObligationsDto::getPaymentDate));
            dataProvider.refreshAll();
            UIUtils.showNotificationType("Obligación Finalizada","success");
        });

        return btn;
    }

    private void applyFilter(ListDataProvider<ObligationsDto> dataProvider){
        dataProvider.clearFilters();
        if(!nameSupplierFilter.getValue().trim().equals("")){
            dataProvider.addFilter(obligationsDto ->
                    StringUtils.containsIgnoreCase(obligationsDto.getNameSupplier(),
                            nameSupplierFilter.getValue().trim()));
        }
        if(!typeObligationFilter.getValue().trim().equals("")){
            dataProvider.addFilter(obligationsDto ->
                    StringUtils.containsIgnoreCase(obligationsDto.getTypeObligation(),
                            typeObligationFilter.getValue().trim()));
        }
        if(!periodFilter.getValue().trim().equals("")){
            dataProvider.addFilter(obligationsDto ->
                    StringUtils.containsIgnoreCase(obligationsDto.getPeriod(),
                            periodFilter.getValue().trim()));
        }
        if(!typeDocumentReceivedFilter.getValue().trim().equals("")){
            dataProvider.addFilter(obligationsDto ->
                    StringUtils.containsIgnoreCase(obligationsDto.getTypeDocumentReceived(),
                            typeDocumentReceivedFilter.getValue().trim()));
        }
        if(!stateFilter.getValue().trim().equals("")){
            dataProvider.addFilter(obligationsDto ->
                    StringUtils.containsIgnoreCase(obligationsDto.getState(),
                            stateFilter.getValue().trim()));
        }

    }

    private void getListObligations(){
        if(VaadinSession.getCurrent().getAttribute("scope").toString().equals("NACIONAL")) {
            obligationsDtoList = new ArrayList<>(restTemplate.getAll());
        }else{
            obligationsDtoList = new ArrayList<>(restTemplate.getByCreatedByAndState(VaadinSession.getCurrent().getAttribute("login").toString()));
        }

        dataProvider = new ListDataProvider<>(obligationsDtoList);
    }
}
