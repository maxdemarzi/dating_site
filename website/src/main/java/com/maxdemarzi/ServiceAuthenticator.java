package com.maxdemarzi;

import com.maxdemarzi.models.User;
import org.mindrot.jbcrypt.BCrypt;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.exception.*;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.util.CommonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Response;

import javax.inject.Provider;
import java.io.IOException;

public class ServiceAuthenticator implements Authenticator<UsernamePasswordCredentials> {
    private static final Logger logger = LoggerFactory.getLogger(ServiceAuthenticator.class);
    private final Provider<API> api;

    public ServiceAuthenticator(Provider<API> api) {
        this.api = api;
    }

    @Override
    public void validate(UsernamePasswordCredentials credentials, WebContext webContext) throws HttpAction, CredentialsException {
        if (credentials == null) {
            throwException("No credentials");
            return;
        }

        String username = credentials.getUsername();
        if (CommonHelper.isBlank(username)) {
            throwException("Username cannot be blank");
        }

        String password = credentials.getPassword();
        if (CommonHelper.isBlank(password)) {
            throwException("Password cannot be blank");
        }

        Response<User> response;
        try {
            response = api.get().getUser(username).execute();
            User user = response.body();
            if (user == null || !BCrypt.checkpw(credentials.getPassword(), user.getPassword())){
                String message = "Bad credentials for: " + username;
                logger.error(message);
                throw new BadCredentialsException(message);
            } else {
                CommonProfile profile = new CommonProfile();
                profile.addAttribute("username", username);
                profile.setId(username);
                profile.addAttribute("name", user.getName());
                profile.addAttribute("email", user.getEmail());
                credentials.setUserProfile(profile);
            }
        } catch (IOException e) {
            String message = "No account found for: " + username;
            logger.error(message);
            throw new AccountNotFoundException(message);
        }
    }

    private void throwException(String message) throws CredentialsException {
        logger.error(message);
        throw new CredentialsException(message);
    }
}
