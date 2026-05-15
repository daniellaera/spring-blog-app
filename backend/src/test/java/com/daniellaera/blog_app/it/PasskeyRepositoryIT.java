package com.daniellaera.blog_app.it;

import com.daniellaera.blog_app.model.Passkey;
import com.daniellaera.blog_app.model.User;
import com.daniellaera.blog_app.repository.PasskeyRepository;
import com.daniellaera.blog_app.repository.UserRepository;
import com.daniellaera.blog_app.repository.impl.MyCredentialRepository;
import com.daniellaera.blog_app.utils.TestcontainersConfiguration;
import com.yubico.webauthn.data.ByteArray;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class PasskeyRepositoryIT {

    @Autowired PasskeyRepository passkeyRepository;
    @Autowired UserRepository userRepository;

    private User savedUser;
    private final byte[] userHandle = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
    private final byte[] keyId = new byte[]{10, 20, 30, 40, 50};
    private final byte[] publicKey = new byte[]{99, 88, 77};

    @BeforeEach
    void setUp() {
        passkeyRepository.deleteAll();
        userRepository.deleteAll();

        User user = new User();
        user.setUsername("repo-test-user");
        user.setUserHandle(userHandle);
        user.setRegistrationComplete(true);
        savedUser = userRepository.save(user);
    }

    @Test
    void saveAndRetrieveByKeyId() {
        Passkey passkey = buildPasskey();
        passkeyRepository.save(passkey);

        List<Passkey> found = passkeyRepository.findAllByKeyId(keyId);

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getKeyId()).isEqualTo(keyId);
    }

    @Test
    void saveAndRetrieveByUserId() {
        Passkey passkey = buildPasskey();
        passkeyRepository.save(passkey);

        List<Passkey> found = passkeyRepository.findByUserId(savedUser.getId());

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getUserId()).isEqualTo(savedUser.getId());
    }

    @Test
    void lookupUnknownCredentialId_returnsEmpty() {
        MyCredentialRepository credentialRepo =
                new MyCredentialRepository(userRepository, passkeyRepository);

        ByteArray unknownCredId = new ByteArray(new byte[]{99, 98, 97});
        ByteArray someUserHandle = new ByteArray(userHandle);

        Optional<com.yubico.webauthn.RegisteredCredential> result =
                credentialRepo.lookup(unknownCredId, someUserHandle);

        assertThat(result).isEmpty();
    }

    private Passkey buildPasskey() {
        Passkey passkey = new Passkey();
        passkey.setUserHandle(userHandle);
        passkey.setKeyId(keyId);
        passkey.setPublicKey(publicKey);
        passkey.setType("public-key");
        passkey.setTransport("internal");
        passkey.setSignatureCount(0L);
        passkey.setUserId(savedUser.getId());
        passkey.setDeviceName("Test device");
        return passkey;
    }
}
