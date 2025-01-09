package org.cpilint.rules;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.cpilint.IflowXml;
import org.cpilint.artifacts.IflowArtifact;
import org.cpilint.issues.UserRoleIssue;
import org.cpilint.model.SenderAdapter;
import org.cpilint.model.XmlModel;
import org.cpilint.model.XmlModelFactory;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmValue;

public final class UserRolesRule extends RuleBase {

    private final boolean isAllowList;
    private final Set<String> userRoles;

    UserRolesRule(boolean isAllowList, Set<String> userRoles) {
        assert userRoles != null;
        assert !userRoles.isEmpty();
        assert userRoles.stream().noneMatch(u -> u == null);
        assert userRoles.stream().noneMatch(u -> u.isEmpty() || u.isBlank());
        this.isAllowList = isAllowList;
        this.userRoles = new HashSet<>(userRoles);
    }

    @Override
    public void inspect(IflowArtifact iflow) {
        /*
         * Why can't we just retrieve the XQuery once and reuse it for every
         * iflow? At the moment we could, but in the future the XmlModel might
         * vary depending on the provided iflow artifact. In that scenario, the
         * XQuery might also vary.
         */
        final IflowXml iflowXml = iflow.getIflowXml();
		final XmlModel model = XmlModelFactory.getModelFor(iflowXml);
		final String xquery = model.xqueryForSenderChannelUserRoles();
		final XdmValue result = iflowXml.executeXquery(xquery);
		/*
		 * The returned sequence must either be empty, or the number of
		 * elements must be a multiple of four (since the sequence
		 * consists of tuples of channel name, channel id, adapter component
         * type and user role).
		 */        
		if (!(result.size() == 0 || result.size() % 4 == 0)) {
			throw new RuleError(String.format("Unexpected size (%d) of sequence returned by XQuery query", result.size()));
		}
        /*
         * Now process the sequence four elements at a time (= one sender channel with
         * user role authorization at a time). If a channel is configured with a user
         * role that is not allowed, an issue is created.
         */
        final Iterator<XdmItem> sequenceIterator = result.iterator();
        while (sequenceIterator.hasNext()) {
            final String channelName = sequenceIterator.next().getStringValue();
            final String channelId = sequenceIterator.next().getStringValue();
            final String adapterComponentType = sequenceIterator.next().getStringValue();
            final String userRole = sequenceIterator.next().getStringValue();
            final boolean userRoleNotInAllowList = isAllowList && !userRoles.contains(userRole);
            final boolean userRoleInDisallowList = !isAllowList && userRoles.contains(userRole);
            final SenderAdapter senderAdapter = model.senderChannelComponentTypeToSenderAdapter(adapterComponentType);
            if (userRoleNotInAllowList || userRoleInDisallowList) {
                consumer.consume(new UserRoleIssue(
                    iflow.getTag(),
                    senderAdapter,
                    channelName,
                    channelId,
                    userRole
                ));
            }
        }
    }

}
