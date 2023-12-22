/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.collection.UnmodifiableSetDecorator;
import ch.systemsx.cisd.common.reflection.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentityHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings;
import ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.SearchFieldConstants;

/**
 * A <i>Persistence Entity</i> which represents a person.
 * 
 * @author Christian Ribeaud
 */
@Entity
@Table(name = TableNames.PERSONS_TABLE, uniqueConstraints =
{ @UniqueConstraint(columnNames =
{ ColumnNames.USER_COLUMN }) })
@Friend(toClasses = RoleAssignmentPE.class)
public final class PersonPE extends HibernateAbstractRegistrationHolder implements
        Comparable<PersonPE>, IIdentityHolder, Serializable
{
    private static final long serialVersionUID = IServer.VERSION;

    /** Regular expression for allowed user codes */
    private static final String USER_CODE_REGEX = "^([a-zA-Z0-9_\\.\\-\\@])+$";

    /**
     * The user id of the system user.
     */
    public static final String SYSTEM_USER_ID = "system";

    private SpacePE homeSpace;

    private transient Long id;

    private String userId;

    private String firstName;

    private String lastName;

    private String email;

    private boolean systemUser;

    private Set<RoleAssignmentPE> roleAssignments = new HashSet<RoleAssignmentPE>();

    private Set<AuthorizationGroupPE> authorizationGroups = new HashSet<AuthorizationGroupPE>();

    private boolean active;

    private PersonDisplaySettingsPE personDisplaySettings;

    private final void setSystemUser(final boolean systemUser)
    {
        this.systemUser = systemUser;
    }

    @Column(name = ColumnNames.FIRST_NAME_COLUMN)
    public final String getFirstName()
    {
        return firstName;
    }

    public final void setFirstName(final String firstName)
    {
        this.firstName = firstName;
    }

    @Column(name = ColumnNames.LAST_NAME_COLUMN)
    public final String getLastName()
    {
        return lastName;
    }

    public final void setLastName(final String lastName)
    {
        this.lastName = lastName;
    }

    @Email(message = ValidationMessages.EMAIL_EMAIL_MESSAGE)
    @Length(max = 320, message = ValidationMessages.EMAIL_LENGTH_MESSAGE)
    public final String getEmail()
    {
        return email;
    }

    public final void setEmail(final String email)
    {
        this.email = email;
    }

    @Column(name = ColumnNames.USER_COLUMN)
    @Length(max = 256, message = ValidationMessages.USER_ID_LENGTH_MESSAGE)
    @NotNull(message = ValidationMessages.USER_ID_NOT_NULL_MESSAGE)
    @Pattern(regexp = USER_CODE_REGEX, flags = Pattern.Flag.CASE_INSENSITIVE, message = ValidationMessages.VALID_USER_CODE_DESCRIPTION)
    public final String getUserId()
    {
        return userId;
    }

    public final void setUserId(final String userId)
    {
        this.userId = userId;
        setSystemUser(SYSTEM_USER_ID.equals(userId));
    }

    @Transient
    public final boolean isSystemUser()
    {
        return systemUser;
    }

    public final void setId(final Long id)
    {
        this.id = id;
    }

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = ColumnNames.ID_COLUMN, nullable = true)
    public final PersonDisplaySettingsPE getPersonDisplaySettings()
    {
        return this.personDisplaySettings;
    }

    public final void setPersonDisplaySettings(PersonDisplaySettingsPE personDisplaySettings)
    {
        this.personDisplaySettings = personDisplaySettings;
    }

    @Transient
    public DisplaySettings getDisplaySettings()
    {
        if (getPersonDisplaySettings() == null)
        {
            setPersonDisplaySettings(new PersonDisplaySettingsPE());
        }
        return getPersonDisplaySettings().getDisplaySettings();
    }

    public void setDisplaySettings(DisplaySettings displaySettings)
    {
        if (getPersonDisplaySettings() == null)
        {
            setPersonDisplaySettings(new PersonDisplaySettingsPE());
        }
        getPersonDisplaySettings().setDisplaySettings(displaySettings);
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.SPACE_COLUMN)
    public final SpacePE getHomeSpace()
    {
        return homeSpace;
    }

    public final void setHomeSpace(final SpacePE homeSpace)
    {
        this.homeSpace = homeSpace;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "personInternal")
    private final Set<RoleAssignmentPE> getRoleAssignmentsInternal()
    {
        return roleAssignments;
    }

    // Required by Hibernate.
    @SuppressWarnings("unused")
    private final void setRoleAssignmentsInternal(final Set<RoleAssignmentPE> rolesAssignments)
    {
        this.roleAssignments = rolesAssignments;
    }

    public final void setRoleAssignments(final Set<RoleAssignmentPE> rolesAssignments)
    {
        getRoleAssignmentsInternal().clear();
        for (final RoleAssignmentPE child : rolesAssignments)
        {
            addRoleAssignment(child);
        }
    }

    @Transient
    public final Set<RoleAssignmentPE> getRoleAssignments()
    {
        return new UnmodifiableSetDecorator<RoleAssignmentPE>(getRoleAssignmentsInternal());
    }

    public void addRoleAssignment(final RoleAssignmentPE roleAssignment)
    {
        final PersonPE person = roleAssignment.getPerson();
        if (person != null)
        {
            person.removeRoleAssigment(roleAssignment);
        }
        roleAssignment.setPersonInternal(this);
        getRoleAssignmentsInternal().add(roleAssignment);
    }

    public void removeRoleAssigment(final RoleAssignmentPE roleAssignment)
    {
        assert roleAssignment != null : "Unspecified role assignment.";
        getRoleAssignmentsInternal().remove(roleAssignment);
        roleAssignment.setPersonInternal(null);
    }

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = TableNames.AUTHORIZATION_GROUP_PERSONS_TABLE, joinColumns =
    { @JoinColumn(name = ColumnNames.PERSON_ID_COLUMN, updatable = false) }, inverseJoinColumns =
    { @JoinColumn(name = ColumnNames.AUTHORIZATION_GROUP_ID_COLUMN, updatable = false) })
    final Set<AuthorizationGroupPE> getAuthorizationGroupsInternal()
    {
        return authorizationGroups;
    }

    // Required by Hibernate.
    @SuppressWarnings("unused")
    private final void setAuthorizationGroupsInternal(
            final Set<AuthorizationGroupPE> authorizationGroups)
    {
        this.authorizationGroups = authorizationGroups;
    }

    @Transient
    public final void clearAuthorizationGroups()
    {
        authorizationGroups.clear();
    }

    @Transient
    public final Set<AuthorizationGroupPE> getAuthorizationGroups()
    {
        return new UnmodifiableSetDecorator<AuthorizationGroupPE>(getAuthorizationGroupsInternal());
    }

    @Column(name = ColumnNames.PERSON_IS_ACTIVE_COLUMN)
    public boolean isActive()
    {
        return active;
    }

    public void setActive(boolean isActive)
    {
        this.active = isActive;
    }

    //
    // IIdentifierHolder
    //
    @Override
    @SequenceGenerator(name = SequenceNames.PERSON_SEQUENCE, sequenceName = SequenceNames.PERSON_SEQUENCE, allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.PERSON_SEQUENCE)
    public final Long getId()
    {
        return id;
    }

    @Override
    @Transient
    public String getIdentifier()
    {
        return getPermId();
    }
    
    @Override
    @Transient
    public String getPermId()
    {
        return getUserId();
    }
    
    //
    // Object
    //

    @Override
    public final boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof PersonPE == false)
        {
            return false;
        }
        final PersonPE that = (PersonPE) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(getUserId(), that.getUserId());
        return builder.isEquals();
    }

    @Override
    public final int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getUserId());
        return builder.toHashCode();
    }

    @Override
    public final String toString()
    {
        final ToStringBuilder builder =
                new ToStringBuilder(this,
                        ModifiedShortPrefixToStringStyle.MODIFIED_SHORT_PREFIX_STYLE);
        builder.append("userId", userId);
        builder.append("firstName", firstName);
        builder.append("lastName", lastName);
        builder.append("email", email);
        builder.append("systemUser", systemUser);
        if (getPersonDisplaySettings() != null)
        {
            builder.append("displaySettings", "<"
                    + getPersonDisplaySettings().getDisplaySettingsSize() + " bytes>");
        }
        return builder.toString();
    }

    //
    // Comparable
    //

    /**
     * If <code>null</code> values are present for <code>code</code>, then they come first.
     */
    @Override
    public final int compareTo(final PersonPE o)
    {
        final String thatUserID = o.userId;
        if (userId == null)
        {
            return thatUserID == null ? 0 : -1;
        }
        if (thatUserID == null)
        {
            return 1;
        }
        return userId.compareTo(thatUserID);
    }

    @Transient
    public Set<RoleAssignmentPE> getAllPersonRoles()
    {
        HashSet<RoleAssignmentPE> result = new HashSet<RoleAssignmentPE>(getRoleAssignments());
        for (AuthorizationGroupPE ag : getAuthorizationGroups())
        {
            result.addAll(ag.getRoleAssignments());
        }
        return result;
    }
}
