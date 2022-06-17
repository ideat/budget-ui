package com.mindware.ui.views.acquisition;

import com.mindware.backend.entity.adquisition.Acquisition;
import com.mindware.backend.entity.adquisition.AcquisitionDto;
import com.mindware.backend.rest.acquisition.AcquisitionDtoRestTemplate;
import com.mindware.backend.rest.acquisition.AcquisitionRestTemplate;
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
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.*;
import dev.mett.vaadin.tooltip.Tooltips;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Route(value = "acquisition", layout = MainLayout.class)
@ParentLayout(MainLayout.class)
@PageTitle("Adquisiciones")
public class AcquisitionView   extends ViewFrame implements RouterLayout {

    @Autowired
    private AcquisitionDtoRestTemplate restTemplate;

    @Autowired
    private AcquisitionRestTemplate acquisitionRestTemplate;

    private ListDataProvider<AcquisitionDto> dataProvider;

    private List<AcquisitionDto> acquisitionDtoList;

    private Button btnNew;

    private TextField acquisitionNumberFilter;
    private TextField supplierFilter;
    private DatePicker receptionDateInitFilter;
    private DatePicker receptionDateEndFilter;
    private TextField itemFilter;
    private TextField stateFilter;
    private TextField nameBusinessUnitFilter;

    private Grid<AcquisitionDto> grid;

    @Override
    protected void onAttach(AttachEvent attachEvent){
        super.onAttach(attachEvent);
        getAcquisitionDto();
        setViewHeader(createTopBar());
        setViewContent(createContent());
    }

    private HorizontalLayout createTopBar(){
        btnNew = new Button("Nueva Adquisición");
        btnNew.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnNew.setIcon(VaadinIcon.PLUS_CIRCLE.create());
        btnNew.addClickShortcut(Key.KEY_N, KeyModifier.ALT);
        btnNew.addClickListener(e -> {
            Map<String, List<String>> param = new HashMap<>();
            List<String> id = new ArrayList<>();
            id.add("NUEVO");

            param.put("id",id);

            QueryParameters qp = new QueryParameters(param);
            UI.getCurrent().navigate("acquisition-register",qp);
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
        FlexBoxLayout content = new FlexBoxLayout(createGridAcquisitionDto());
        content.addClassName("grid-view");
        content.setHeightFull();
        content.setBoxSizing(BoxSizing.BORDER_BOX);
        content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);

        return content;

    }

    private Grid createGridAcquisitionDto(){
        grid = new Grid<>();
        grid.addThemeVariants(GridVariant.LUMO_COMPACT);
        grid.setMultiSort(true);
        grid.setSizeFull();
        grid.setDataProvider(dataProvider);

        grid.addColumn(AcquisitionDto::getAcquisitionNumber)
                .setFlexGrow(1)
                .setKey("acquisitionNumber")
                .setHeader("Nro. Adquisición")
                .setSortable(true)
                .setAutoWidth(true)
                .setTextAlign(ColumnTextAlign.CENTER)
                .setResizable(true);
        grid.addColumn(AcquisitionDto::getSupplier)
                .setFlexGrow(1)
                .setKey("supplier")
                .setHeader("Proveedor")
                .setSortable(true)
                .setAutoWidth(true)
                .setResizable(true);
        grid.addColumn(AcquisitionDto::getNameBusinessUnit)
                .setFlexGrow(1)
                .setKey("nameBusinessUnit")
                .setHeader("Unidad Negocio")
                .setSortable(true)
                .setAutoWidth(true)
                .setResizable(true);
        grid.addColumn(new LocalDateRenderer<>(AcquisitionDto::getReceptionDate
                , DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .setKey("receptionDate")
                .setHeader("Fecha Recepción")
                .setFlexGrow(1)
                .setAutoWidth(true)
                .setSortable(true)
                .setTextAlign(ColumnTextAlign.CENTER)
                .setResizable(true);
        grid.addColumn(new ComponentRenderer<>(this::createItem))
                .setFlexGrow(1)
                .setKey("item")
                .setHeader("Ítems")
                .setAutoWidth(true)
                .setResizable(true);
        grid.addColumn(AcquisitionDto::getState)
                .setFlexGrow(1)
                .setKey("state")
                .setHeader("Estado")
                .setSortable(true)
                .setAutoWidth(true)
                .setResizable(true);
        grid.addColumn(new ComponentRenderer<>(this::createButtonEdit))
                .setFlexGrow(1)
                .setAutoWidth(true);
        grid.addColumn(new ComponentRenderer<>(this::createButtonSend))
                .setFlexGrow(1)
                .setAutoWidth(true);
        grid.addColumn(new ComponentRenderer<>(this::createButtonRegard))
                .setFlexGrow(1)
                .setAutoWidth(true);
        grid.addColumn(new ComponentRenderer<>(this::createButtonFinish))
                .setFlexGrow(1)
                .setAutoWidth(true);

        HeaderRow hr = grid.appendHeaderRow();

        acquisitionNumberFilter = new TextField();
        acquisitionNumberFilter.setValueChangeMode(ValueChangeMode.EAGER);
        acquisitionNumberFilter.setWidthFull();
        acquisitionNumberFilter.addValueChangeListener(e -> applyFilter(dataProvider));
        hr.getCell(grid.getColumnByKey("acquisitionNumber")).setComponent(acquisitionNumberFilter);

        supplierFilter = new TextField();
        supplierFilter.setValueChangeMode(ValueChangeMode.EAGER);
        supplierFilter.setWidthFull();
        supplierFilter.addValueChangeListener(e -> applyFilter(dataProvider));
        hr.getCell(grid.getColumnByKey("supplier")).setComponent(supplierFilter);

        nameBusinessUnitFilter = new TextField();
        nameBusinessUnitFilter.setValueChangeMode(ValueChangeMode.EAGER);
        nameBusinessUnitFilter.setWidthFull();
        nameBusinessUnitFilter.addValueChangeListener(e -> applyFilter(dataProvider));
        hr.getCell(grid.getColumnByKey("nameBusinessUnit")).setComponent(nameBusinessUnitFilter);

        receptionDateInitFilter = new DatePicker();
        receptionDateInitFilter.setWidth("40%");
        receptionDateInitFilter.setLocale(new Locale("es","BO"));
        receptionDateInitFilter.setClearButtonVisible(true);
        receptionDateInitFilter.addValueChangeListener(e -> {
            applyFilter(dataProvider);
        });

        receptionDateEndFilter = new DatePicker();
        receptionDateEndFilter.setWidth("40%");
        receptionDateEndFilter.setLocale(new Locale("es","BO"));
        receptionDateEndFilter.setClearButtonVisible(true);
        receptionDateEndFilter.addValueChangeListener(e -> {
            applyFilter(dataProvider);
        });
        HorizontalLayout layoutFilterDate = new HorizontalLayout();
        layoutFilterDate.add(receptionDateInitFilter,receptionDateEndFilter);
        hr.getCell(grid.getColumnByKey("receptionDate")).setComponent(layoutFilterDate);

        itemFilter = new TextField();
        itemFilter.setValueChangeMode(ValueChangeMode.EAGER);
        itemFilter.setWidthFull();
        itemFilter.addValueChangeListener(e -> applyFilter(dataProvider));
        hr.getCell(grid.getColumnByKey("item")).setComponent(itemFilter);

        stateFilter = new TextField();
        stateFilter.setValueChangeMode(ValueChangeMode.EAGER);
        stateFilter.setWidthFull();
        stateFilter.addValueChangeListener(e -> applyFilter(dataProvider));
        hr.getCell(grid.getColumnByKey("state")).setComponent(stateFilter);

        return grid;
    }

    private Component createItem(AcquisitionDto acquisitionDto){
        TextArea textArea = new TextArea();
        textArea.setWidthFull();
        textArea.setReadOnly(true);
        textArea.setValue(acquisitionDto.getItems());
        return textArea;
    }

    private Component createButtonEdit(AcquisitionDto acquisitionDto){
        Button btn = new Button();
        Tooltips.getCurrent().setTooltip(btn,"Editar Registro");
        btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_SUCCESS);
        btn.setIcon(VaadinIcon.EDIT.create());
        btn.addClickListener(event -> {
            Map<String, List<String>> param = new HashMap<>();
            List<String> id = new ArrayList<>();
            List<String> numberRequest = new ArrayList<>();
            id.add(acquisitionDto.getId().toString());
            numberRequest.add(acquisitionDto.getAcquisitionNumber().toString());
            param.put("id",id);
            param.put("numberRequest",numberRequest);

            QueryParameters qp = new QueryParameters(param);
            UI.getCurrent().navigate("acquisition-register",qp);
        });
        return btn;
    }

    private Component createButtonSend(AcquisitionDto acquisitionDto){
        Button btn = new Button();
        Tooltips.getCurrent().setTooltip(btn,"Enviar");
        btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btn.setIcon(VaadinIcon.THUMBS_UP.create());
        btn.setVisible(GrantOptions.grantedOptionSend("Adquisiciones"));
        btn.addClickListener(event -> {
            if(acquisitionDto.getDateDeliveryAccounting()==null){
                UIUtils.showNotificationType("Registre fecha de envio a contabilidad","alert");
                return;
            }
            Acquisition acquisition = new Acquisition();
            acquisition.setId(acquisitionDto.getId());
            acquisition.setState("ENVIADO");
            acquisitionRestTemplate.udpateState(acquisition);
            acquisitionDto.setState("ENVIADO");
            acquisitionDtoList.removeIf(ac -> ac.getId().equals(acquisitionDto.getId()));
            acquisitionDtoList.add(acquisitionDto);
            acquisitionDtoList.sort(Comparator.comparing(AcquisitionDto::getReceptionDate));
            dataProvider.refreshAll();
            UIUtils.showNotificationType("Enviado a Contabilidad","success");
        });
        return btn;
    }

    private Component createButtonFinish(AcquisitionDto acquisitionDto){
        Button btn = new Button();
        Tooltips.getCurrent().setTooltip(btn,"Finalizar");
        btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_CONTRAST);
        btn.setIcon(VaadinIcon.LOCK.create());
        btn.setVisible(GrantOptions.grantedOptionFinish("Adquisiciones"));
        btn.addClickListener(event -> {
            if(!acquisitionDto.getState().equals("ENVIADO")){
                UIUtils.showNotificationType("No puede finalizar sin estar ENVIADO","alert");
                return;
            }
            if(acquisitionDto.getDateDeliveryAccounting()==null){
                UIUtils.showNotificationType("No puede finalizar sin enviar a Contabilidad","alert");
                return;
            }
            Acquisition acquisition = new Acquisition();
            acquisition.setId(acquisitionDto.getId());
            acquisition.setState("FINALIZADO");
            acquisitionRestTemplate.udpateState(acquisition);
            acquisitionDto.setState("FINALIZADO");
            acquisitionDtoList.removeIf(ac -> ac.getId().equals(acquisitionDto.getId()));
            acquisitionDtoList.add(acquisitionDto);
            acquisitionDtoList.sort(Comparator.comparing(AcquisitionDto::getReceptionDate));
            grid.getDataProvider().refreshAll();

            UIUtils.showNotificationType("Adquisición Finalizada","success");
        });

        return btn;
    }

    private Component createButtonRegard(AcquisitionDto acquisitionDto){
        Button btn = new Button();
        Tooltips.getCurrent().setTooltip(btn,"Observado");
        btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        btn.setIcon(VaadinIcon.THUMBS_DOWN_O.create());
        btn.setVisible(GrantOptions.grantedOptionObserved("Adquisiciones"));
        btn.addClickListener(event -> {
            Acquisition acquisition = new Acquisition();
            acquisition.setId(acquisitionDto.getId());
            acquisition.setState("OBSERVADO");
            acquisitionRestTemplate.udpateState(acquisition);
            acquisitionDto.setState("OBSERVADO");
            acquisitionDtoList.removeIf(ac -> ac.getId().equals(acquisitionDto.getId()));
            acquisitionDtoList.add(acquisitionDto);
            acquisitionDtoList.sort(Comparator.comparing(AcquisitionDto::getReceptionDate));
            grid.getDataProvider().refreshAll();
            UIUtils.showNotificationType("Adquisición Observada","alert");
        });
        return btn;
    }

    private void applyFilter(ListDataProvider<AcquisitionDto> dataProvider){
        dataProvider.clearFilters();
        if(!acquisitionNumberFilter.getValue().trim().equals("")){
            dataProvider.addFilter(acquisitionDto -> StringUtils.containsIgnoreCase(
                    acquisitionDto.getAcquisitionNumber(),acquisitionNumberFilter.getValue()));
        }
        if(!supplierFilter.getValue().trim().equals("")){
            dataProvider.addFilter(acquisitionDto -> StringUtils.containsIgnoreCase(
                    acquisitionDto.getSupplier(),supplierFilter.getValue()));
        }
        if(receptionDateInitFilter.getValue()!=null){
            dataProvider.addFilter(contractDto -> contractDto.getReceptionDate().isAfter(receptionDateInitFilter.getValue()) ||
                    contractDto.getReceptionDate().isEqual(receptionDateInitFilter.getValue()));
        }
        if(receptionDateEndFilter.getValue()!=null){
            dataProvider.addFilter(contractDto -> contractDto.getReceptionDate().isBefore(receptionDateEndFilter.getValue()) ||
                    contractDto.getReceptionDate().isEqual(receptionDateEndFilter.getValue()));
        }
        if(!itemFilter.getValue().trim().equals("")){
            dataProvider.addFilter(acquisitionDto -> StringUtils.containsIgnoreCase(
                    acquisitionDto.getItems(),itemFilter.getValue()));
        }
        if(!stateFilter.getValue().trim().equals("")){
            dataProvider.addFilter(acquisitionDto -> StringUtils.containsIgnoreCase(
                    acquisitionDto.getState(),stateFilter.getValue()));
        }
        if(!nameBusinessUnitFilter.getValue().trim().equals("")){
            dataProvider.addFilter(acquisitionDto -> StringUtils.containsIgnoreCase(
                    acquisitionDto.getNameBusinessUnit(),nameBusinessUnitFilter.getValue()));
        }
    }

    private void getAcquisitionDto(){
        acquisitionDtoList = new ArrayList<>(restTemplate.getAll());
        dataProvider = new ListDataProvider<>(acquisitionDtoList);
    }
}
