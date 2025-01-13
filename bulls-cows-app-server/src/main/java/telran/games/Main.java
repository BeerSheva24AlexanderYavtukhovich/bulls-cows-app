package telran.games;

import java.util.HashMap;
import java.util.Scanner;

import org.hibernate.jpa.HibernatePersistenceProvider;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.spi.PersistenceUnitInfo;
import telran.games.configs.BullsCowsPersistenceUnitInfo;
import telran.games.configs.ServerConfig;
import telran.games.db.repository.BullsCowsRepository;
import telran.games.db.repository.BullsCowsRepositoryImpl;
import telran.games.service.BullsCowsProtocol;
import telran.games.service.BullsCowsService;
import telran.games.service.BullsCowsServiceImpl;
import telran.net.TcpServer;

public class Main {
    static EntityManager em;

    public static void main(String[] args) {
        createEm();
        BullsCowsRepository repository = new BullsCowsRepositoryImpl(em);
        BullsCowsService service = new BullsCowsServiceImpl(repository);

        TcpServer tcpServer = new TcpServer(new BullsCowsProtocol(service), ServerConfig.PORT);
        new Thread(tcpServer).start();
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.println("Enter shutdown for stopping server");
                String line = scanner.nextLine();
                if (line.equals("shutdown")) {
                    tcpServer.shutdown();
                    break;
                }
            }
        }
    }

    private static void createEm() {
        HashMap<String, Object> hibernateProperties = new HashMap<>();
        hibernateProperties.put("hibernate.hbm2ddl.auto", "update");
        PersistenceUnitInfo persistanceUnit = new BullsCowsPersistenceUnitInfo();
        HibernatePersistenceProvider hibernatePersistenceProvider = new HibernatePersistenceProvider();
        EntityManagerFactory emf = hibernatePersistenceProvider.createContainerEntityManagerFactory(persistanceUnit,
                hibernateProperties);
        em = emf.createEntityManager();
    }
}