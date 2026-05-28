    package com.example.UCTalen.config;

    import com.google.auth.oauth2.GoogleCredentials;
    import com.google.firebase.FirebaseApp;
    import com.google.firebase.FirebaseOptions;
    import jakarta.annotation.PostConstruct;
    import org.springframework.context.annotation.Configuration;

    import java.io.ByteArrayInputStream;
    import java.io.FileInputStream;
    import java.io.InputStream;
    import java.nio.charset.StandardCharsets;

    @Configuration
    public class FirebaseConfig {

            @PostConstruct
            public void initialize() {
                try {

                    if (FirebaseApp.getApps().isEmpty()) {

                        String firebaseConfigJson = System.getenv("FIREBASE_CONFIG_JSON");

                        InputStream serviceAccount;

                        if (firebaseConfigJson != null && !firebaseConfigJson.isBlank()) {

                            System.out.println(">> Đang dùng Firebase ENV trên Render...");

                            serviceAccount = new ByteArrayInputStream(
                                    firebaseConfigJson.getBytes(StandardCharsets.UTF_8)
                            );

                        } else {

                            System.out.println(">> Đang dùng file local serviceAccountKey.json");

                            serviceAccount = new FileInputStream("serviceAccountKey.json");
                        }

                        FirebaseOptions options = FirebaseOptions.builder()
                                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                                .build();

                        FirebaseApp.initializeApp(options);

                        System.out.println("====== FIREBASE KẾT NỐI THÀNH CÔNG ======");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
