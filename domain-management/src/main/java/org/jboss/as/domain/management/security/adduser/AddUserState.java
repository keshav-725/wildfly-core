/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.as.domain.management.security.adduser;

import org.jboss.as.domain.management.logging.DomainManagementLogger;

/**
 * State to perform the actual addition to the discovered properties files.
 * <p/>
 * By this time ALL validation should be complete, this State will only fail for IOExceptions encountered
 * performing the actual writes.
 */
public class AddUserState extends UpdatePropertiesHandler implements State {

    private final StateValues stateValues;
    private final ConsoleWrapper theConsole;

    public AddUserState(ConsoleWrapper theConsole, final StateValues stateValues) {
        super(theConsole);
        this.theConsole = theConsole;
        this.stateValues = stateValues;
    }

    @Override
    public State execute() {
        final String password = stateValues.getPassword();
        State nextState;
        if (password == null) {
            // The user doesn't exist and the password is not provided !
            nextState = new ErrorState(theConsole, DomainManagementLogger.ROOT_LOGGER.noPasswordExiting(), null, stateValues);
        } else {
            nextState = update(stateValues);
        }

        return nextState;
    }

    @Override
    String consoleUserMessage(String filePath) {
        return DomainManagementLogger.ROOT_LOGGER.addedUser(stateValues.getUserName(), filePath);
    }

    @Override
    String consoleGroupsMessage(String filePath) {
        return DomainManagementLogger.ROOT_LOGGER.addedGroups(stateValues.getUserName(), stateValues.getGroups(), filePath);
    }

    @Override
    String errorMessage(String filePath, Throwable e) {
        return DomainManagementLogger.ROOT_LOGGER.unableToAddUser(filePath, e.getMessage());
    }
}
