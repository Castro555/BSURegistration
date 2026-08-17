package cv.ucc.bsuregistration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Config {

    public static final String HOST = "192.168.88.1";
    public static final String USERNAME = "admin";

    // Lista de passwords a tentar, por ordem, ao autenticar nos equipamentos.
    // Todas as classes (LeituraBSU, LeituraBSUether, MultiThreadsProcessor) devem
    // ler esta lista em vez de manterem cada uma a sua própria cópia.
    public static final List<String> PASSWORDS = Collections.unmodifiableList(
            Arrays.asList(
                    "TRM@Unitel@123",
                    "admin",
                    ""
            )
    );
}
