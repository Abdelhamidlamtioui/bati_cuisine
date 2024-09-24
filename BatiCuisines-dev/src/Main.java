
import main.java.domain.entities.Project;
import main.java.repository.impl.*;
import main.java.service.*;
import main.java.ui.*;

public class Main {
    public static void main(String[] args) {
        ProjectRepository projectRepository =new ProjectRepository();
        ProjectService projectService = new ProjectService(projectRepository);
        ClientRepository clientRepository = new ClientRepository();
        ClientService clientService = new ClientService(clientRepository);
        ClientMenu clientMenu = new ClientMenu(clientService);
        ComponentRepository componentRepository = new ComponentRepository();
        MaterialRepository materialRepository = new MaterialRepository();
        MaterialService materialService = new MaterialService(materialRepository,componentRepository);
        ComponentService componentService =new ComponentService(componentRepository);
        MaterialMenu materialMenu = new MaterialMenu(materialService,componentService);
        WorkForceRepository workForceRepository = new WorkForceRepository();
        WorkForceService workForceService =new WorkForceService(workForceRepository,componentRepository);
        WorkForceMenu workForceMenu = new WorkForceMenu(workForceService,componentService);
        ProjectMenu projectmenu = new ProjectMenu(projectService,clientMenu,materialMenu,workForceMenu);
        DevisRepository devisRepository =new DevisRepository();
        DevisService devisService = new DevisService(devisRepository);
        DevisMenu devisMenu = new DevisMenu(devisService,projectService);
        CostCalculationMenu costCalculationMenu = new CostCalculationMenu(projectRepository,componentRepository,materialService,workForceService,devisService,devisMenu);
        ComponentMenu componentMenu = new ComponentMenu(materialMenu,workForceMenu);
        PrincipalMenu principaleMenu = new PrincipalMenu(projectmenu,devisMenu,clientMenu,costCalculationMenu,componentMenu);
        principaleMenu.menu();
    }
}
