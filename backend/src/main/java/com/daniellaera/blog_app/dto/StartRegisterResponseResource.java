package com.daniellaera.blog_app.dto;

import com.yubico.webauthn.data.AuthenticatorSelectionCriteria;
import com.yubico.webauthn.data.PublicKeyCredentialParameters;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import com.yubico.webauthn.data.UserIdentity;

import java.util.List;

public record StartRegisterResponseResource(String challenge, RelyingPartyIdentity rp, UserIdentity user,
                                            List<PublicKeyCredentialParameters> pubKeyCredParams,
                                            long timeout, String attestation,
                                            List<StartRegisterCredentialResponseResource> excludeCredentials,
                                            AuthenticatorSelectionCriteria authenticatorSelection) {
}
