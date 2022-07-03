package com.mindware.ui;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindware.backend.entity.acquisitionAuthorizer.AcquisitionAuthorizer;
import com.mindware.backend.entity.rol.Option;
import com.mindware.backend.entity.rol.Rol;
import com.mindware.backend.rest.rol.RolRestTemplate;
import com.mindware.ui.views.acquisition.AcquisitionView;
import com.mindware.ui.views.acquisitionAuthorizer.AcquisitionAuthorizerView;
import com.mindware.ui.views.basicServices.BasicServicesView;
import com.mindware.ui.views.config.account.AccountView;
import com.mindware.ui.views.config.basicServiceProvider.BasicServiceProviderView;
import com.mindware.ui.views.config.parameter.ParameterObligationsView;
import com.mindware.ui.views.config.parameter.ParameterView;
import com.mindware.ui.views.config.period.PeriodView;
import com.mindware.ui.views.config.typeChangeCurrency.TypeChangeCurrencyView;
import com.mindware.ui.views.contract.ContractView;
import com.mindware.ui.views.invoiceAuthorizer.InvoiceAuthorizerView;
import com.mindware.ui.views.obligations.ObligationsView;
import com.mindware.ui.views.recurrentService.RecurrentServiceView;
import com.mindware.ui.views.reports.expenseAcquisitions.ExpenseAcquisitionsReport;
import com.mindware.ui.views.reports.expenseServices.ExpenseServicesReport;
import com.mindware.ui.views.reports.investmentBudget.InvestmentBudgetReport;
import com.mindware.ui.views.rol.RolView;
import com.mindware.ui.views.supplier.SupplierView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.page.Viewport;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.server.*;
import com.vaadin.flow.theme.lumo.Lumo;
import com.mindware.ui.components.FlexBoxLayout;
import com.mindware.ui.components.navigation.bar.AppBar;
import com.mindware.ui.components.navigation.bar.TabBar;
import com.mindware.ui.components.navigation.drawer.NaviDrawer;
import com.mindware.ui.components.navigation.drawer.NaviItem;
import com.mindware.ui.components.navigation.drawer.NaviMenu;
import com.mindware.ui.util.UIUtils;
import com.mindware.ui.util.css.Overflow;
import com.mindware.ui.views.Accounts;
import com.mindware.ui.views.Home;
import com.mindware.ui.views.Payments;
import com.mindware.ui.views.Statistics;
import com.mindware.ui.views.personnel.Accountants;
import com.mindware.ui.views.personnel.Managers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@CssImport(value = "./styles/components/charts.css", themeFor = "vaadin-chart", include = "vaadin-chart-default-theme")
@CssImport(value = "./styles/components/floating-action-button.css", themeFor = "vaadin-button")
@CssImport(value = "./styles/components/grid.css", themeFor = "vaadin-grid")
@CssImport("./styles/lumo/border-radius.css")
@CssImport("./styles/lumo/icon-size.css")
@CssImport("./styles/lumo/margin.css")
@CssImport("./styles/lumo/padding.css")
@CssImport("./styles/lumo/shadow.css")
@CssImport("./styles/lumo/spacing.css")
@CssImport("./styles/lumo/typography.css")
@CssImport("./styles/misc/box-shadow-borders.css")
@CssImport(value = "./styles/styles.css", include = "lumo-badge")
@JsModule("@vaadin/vaadin-lumo-styles/badge")
@PWA(name = "Budget", shortName = "Budget", iconPath = UIUtils.IMG_PATH + "logos/18.png", backgroundColor = "#233348", themeColor = "#233348")
@Viewport("width=device-width, minimum-scale=1.0, initial-scale=1.0, user-scalable=yes")
@Route("main")
public class MainLayout extends FlexBoxLayout
		implements RouterLayout, PageConfigurator, AfterNavigationObserver {

	private static final Logger log = LoggerFactory.getLogger(MainLayout.class);
	private static final String CLASS_NAME = "root";


	private Div appHeaderOuter;

	private FlexBoxLayout row;
	private NaviDrawer naviDrawer;
	private FlexBoxLayout column;

	private Div appHeaderInner;
	private FlexBoxLayout viewContainer;
	private Div appFooterInner;

	private Div appFooterOuter;

	private TabBar tabBar;
	private boolean navigationTabs = false;
	private AppBar appBar;

	public List<Option> optionList = new ArrayList<>();

	public MainLayout() {
		VaadinSession.getCurrent()
				.setErrorHandler((ErrorHandler) errorEvent -> {
					log.error("Uncaught UI exception",
							errorEvent.getThrowable());
					Notification.show(
							"We are sorry, but an internal error occurred");
				});

		addClassName(CLASS_NAME);
		setFlexDirection(FlexDirection.COLUMN);
		setSizeFull();
		if(VaadinSession.getCurrent().getAttribute("login") != null) {

			// Initialise the UI building blocks
			initStructure();

			// Populate the navigation drawer
			initNaviItems();

			// Configure the headers and footers (optional)
			initHeadersAndFooters();
		}
	}




	private boolean assignedOption(String name){
		if(optionList.size()==0) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				optionList = mapper.readValue(VaadinSession.getCurrent().getAttribute("options").toString(),
						new TypeReference<List<Option>>() {});
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}

		List<Option> options = optionList.stream().filter(value -> value.getName().equals(name))
				.collect(Collectors.toList());
		return options.get(0).isAssigned();
	}

	/**
	 * Initialise the required components and containers.
	 */
	private void initStructure() {
		naviDrawer = new NaviDrawer();

		viewContainer = new FlexBoxLayout();
		viewContainer.addClassName(CLASS_NAME + "__view-container");
		viewContainer.setOverflow(Overflow.HIDDEN);

		column = new FlexBoxLayout(viewContainer);
		column.addClassName(CLASS_NAME + "__column");
		column.setFlexDirection(FlexDirection.COLUMN);
		column.setFlexGrow(1, viewContainer);
		column.setOverflow(Overflow.HIDDEN);

		row = new FlexBoxLayout(naviDrawer, column);
		row.addClassName(CLASS_NAME + "__row");
		row.setFlexGrow(1, column);
		row.setOverflow(Overflow.HIDDEN);
		add(row);
		setFlexGrow(1, row);
	}

	/**
	 * Initialise the navigation items.
	 */
	private void initNaviItems() {
		NaviMenu menu = naviDrawer.getMenu();
//		menu.addNaviItem(VaadinIcon.HOME, "Home", Home.class);
//		menu.addNaviItem(VaadinIcon.INSTITUTION, "Accounts", Accounts.class);
//		menu.addNaviItem(VaadinIcon.CREDIT_CARD, "Payments", Payments.class);
//		menu.addNaviItem(VaadinIcon.CHART, "Statistics", Statistics.class);
//
//		NaviItem personnel = menu.addNaviItem(VaadinIcon.USERS, "Personnel",
//				null);
//		menu.addNaviItem(personnel, "Accountants", Accountants.class);
//		menu.addNaviItem(personnel, "Managers", Managers.class);

		if(assignedOption("Proveedores")) {
			menu.addNaviItem(VaadinIcon.GROUP, "Proveedores", SupplierView.class);
		}
		if(assignedOption("Contratos")) {
			menu.addNaviItem(VaadinIcon.FILE_TEXT, "Contratos", ContractView.class);
		}
		if(assignedOption("Obligaciones")) {
			menu.addNaviItem(VaadinIcon.INVOICE, "Obligaciones", ObligationsView.class);
		}
		if(assignedOption("Servicios Recurrentes")) {
			menu.addNaviItem(VaadinIcon.GLOBE_WIRE, "Servicios Recurrentes", RecurrentServiceView.class);
		}
		if(assignedOption("Servicios Básicos")) {
			menu.addNaviItem(VaadinIcon.BUILDING, "Servicios Básicos", BasicServicesView.class);
		}
		if(assignedOption("Adquisiciones")) {
			menu.addNaviItem(VaadinIcon.STORAGE, "Adquisiciones", AcquisitionView.class);
		}
		if(assignedOption("Reportes")) {
			NaviItem reports = menu.addNaviItem(VaadinIcon.RECORDS, "Reportes", null);
			if(assignedOption("Control Gastos y Adqui.")) {
				menu.addNaviItem(reports, "Control Gastos y Adqui.", ExpenseAcquisitionsReport.class);
			}
			if(assignedOption("Presupuesto Inversiones")) {
				menu.addNaviItem(reports, "Presupuesto Inversiones", InvestmentBudgetReport.class);
			}
			if(assignedOption("Presupuesto Gastos")) {
				menu.addNaviItem(reports, "Presupuesto Gastos", ExpenseServicesReport.class);
			}
		}
		if(assignedOption("Configuración")) {
			NaviItem configuration = menu.addNaviItem(VaadinIcon.COGS, "Configuración", null);
			if(assignedOption("Parámetros")) {
				menu.addNaviItem(configuration, "Parámetros", ParameterView.class);
			}
			if(assignedOption("Parametro Tipo Obligación")) {
				menu.addNaviItem(configuration, "Parametro Tipo Obligación", ParameterObligationsView.class);
			}
			if(assignedOption("Niveles Aut. Adquisición")) {
				menu.addNaviItem(configuration, "Niveles Aut. Adquisición", AcquisitionAuthorizerView.class);
			}
			if(assignedOption("Autorizadores de Facturas")) {
				menu.addNaviItem(configuration, "Autorizadores de Facturas", InvoiceAuthorizerView.class);
			}
			if(assignedOption("Cuentas")) {
				menu.addNaviItem(configuration, "Cuentas", AccountView.class);
			}
			if(assignedOption("Proveedor Serv. Básicos")) {
				menu.addNaviItem(configuration, "Serv. Básicos y Obligaciones", BasicServiceProviderView.class);
			}
			if(assignedOption("Periodos")) {
				menu.addNaviItem(configuration, "Periodos", PeriodView.class);
			}
			if(assignedOption("Tipo Cambio")) {
				menu.addNaviItem(configuration, "Tipo Cambio", TypeChangeCurrencyView.class);
			}
			if(assignedOption("Roles")) {
				menu.addNaviItem(configuration, "Roles", RolView.class);
			}
		}
	}

	/**
	 * Configure the app's inner and outer headers and footers.
	 */
	private void initHeadersAndFooters() {
		// setAppHeaderOuter();
		// setAppFooterInner();
		// setAppFooterOuter();

		// Default inner header setup:
		// - When using tabbed navigation the view title, user avatar and main menu button will appear in the TabBar.
		// - When tabbed navigation is turned off they appear in the AppBar.

		appBar = new AppBar("");

		// Tabbed navigation
		if (navigationTabs) {
			tabBar = new TabBar();
			UIUtils.setTheme(Lumo.DARK, tabBar);

			// Shift-click to add a new tab
			for (NaviItem item : naviDrawer.getMenu().getNaviItems()) {
				item.addClickListener(e -> {
					if (e.getButton() == 0 && e.isShiftKey()) {
						tabBar.setSelectedTab(tabBar.addClosableTab(item.getText(), item.getNavigationTarget()));
					}
				});
			}
			appBar.getAvatar().setVisible(false);
			setAppHeaderInner(tabBar, appBar);

			// Default navigation
		} else {
			UIUtils.setTheme(Lumo.DARK, appBar);
			setAppHeaderInner(appBar);
		}
	}

	private void setAppHeaderOuter(Component... components) {
		if (appHeaderOuter == null) {
			appHeaderOuter = new Div();
			appHeaderOuter.addClassName("app-header-outer");
			getElement().insertChild(0, appHeaderOuter.getElement());
		}
		appHeaderOuter.removeAll();
		appHeaderOuter.add(components);
	}

	private void setAppHeaderInner(Component... components) {
		if (appHeaderInner == null) {
			appHeaderInner = new Div();
			appHeaderInner.addClassName("app-header-inner");
			column.getElement().insertChild(0, appHeaderInner.getElement());
		}
		appHeaderInner.removeAll();
		appHeaderInner.add(components);
	}

	private void setAppFooterInner(Component... components) {
		if (appFooterInner == null) {
			appFooterInner = new Div();
			appFooterInner.addClassName("app-footer-inner");
			column.getElement().insertChild(column.getElement().getChildCount(),
					appFooterInner.getElement());
		}
		appFooterInner.removeAll();
		appFooterInner.add(components);
	}

	private void setAppFooterOuter(Component... components) {
		if (appFooterOuter == null) {
			appFooterOuter = new Div();
			appFooterOuter.addClassName("app-footer-outer");
			getElement().insertChild(getElement().getChildCount(),
					appFooterOuter.getElement());
		}
		appFooterOuter.removeAll();
		appFooterOuter.add(components);
	}

	@Override
	public void configurePage(InitialPageSettings settings) {
		settings.addMetaTag("apple-mobile-web-app-capable", "yes");
		settings.addMetaTag("apple-mobile-web-app-status-bar-style", "black");
	}

	@Override
	public void showRouterLayoutContent(HasElement content) {
		this.viewContainer.getElement().appendChild(content.getElement());
	}

	public NaviDrawer getNaviDrawer() {
		return naviDrawer;
	}

	public static MainLayout get() {
		return (MainLayout) UI.getCurrent().getChildren()
				.filter(component -> component.getClass() == MainLayout.class)
				.findFirst().get();
	}

	public AppBar getAppBar() {
		return appBar;
	}

	@Override
	public void afterNavigation(AfterNavigationEvent event) {
		if (navigationTabs) {
			afterNavigationWithTabs(event);
		} else {
			afterNavigationWithoutTabs(event);
		}
	}

	private void afterNavigationWithTabs(AfterNavigationEvent e) {
		NaviItem active = getActiveItem(e);
		if (active == null) {
			if (tabBar.getTabCount() == 0) {
				tabBar.addClosableTab("", Home.class);
			}
		} else {
			if (tabBar.getTabCount() > 0) {
				tabBar.updateSelectedTab(active.getText(),
						active.getNavigationTarget());
			} else {
				tabBar.addClosableTab(active.getText(),
						active.getNavigationTarget());
			}
		}
		appBar.getMenuIcon().setVisible(false);
	}

	private NaviItem getActiveItem(AfterNavigationEvent e) {
		for (NaviItem item : naviDrawer.getMenu().getNaviItems()) {
			if (item.isHighlighted(e)) {
				return item;
			}
		}
		return null;
	}

	private void afterNavigationWithoutTabs(AfterNavigationEvent e) {
		NaviItem active = getActiveItem(e);
		if (active != null) {
			getAppBar().setTitle(active.getText());
		}
	}

}
