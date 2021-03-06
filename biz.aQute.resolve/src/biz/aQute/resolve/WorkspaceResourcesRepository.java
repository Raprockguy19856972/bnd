package biz.aQute.resolve;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.osgi.resource.Resource;
import org.osgi.service.repository.ContentNamespace;

import aQute.bnd.build.Project;
import aQute.bnd.build.Workspace;
import aQute.bnd.header.Attrs;
import aQute.bnd.osgi.Domain;
import aQute.bnd.osgi.repository.ResourcesRepository;
import aQute.bnd.osgi.resource.CapabilityBuilder;
import aQute.bnd.osgi.resource.ResourceBuilder;
import aQute.bnd.osgi.resource.ResourceUtils;
import aQute.libg.cryptography.SHA256;
public class WorkspaceResourcesRepository extends ResourcesRepository {

	public static final String WORKSPACE_NAMESPACE = ResourceUtils.WORKSPACE_NAMESPACE;

	public WorkspaceResourcesRepository(Workspace workspace) throws Exception {
		List<Resource> resources = new ArrayList<>();

		for (Project p : workspace.getAllProjects()) {
			File[] files = p.getBuildFiles(false);
			if (files != null) {
				for (File file : files) {
					Domain manifest = Domain.domain(file);
					ResourceBuilder rb = new ResourceBuilder();
					rb.addManifest(manifest);

					Attrs attrs = new Attrs();
					attrs.put(ContentNamespace.CAPABILITY_URL_ATTRIBUTE, file.toURI().toString());
					attrs.putTyped(ContentNamespace.CAPABILITY_SIZE_ATTRIBUTE, file.length());
					attrs.put(ContentNamespace.CONTENT_NAMESPACE, SHA256.digest(file).asHex());

					rb.addCapability(CapabilityBuilder.createCapReqBuilder(ContentNamespace.CONTENT_NAMESPACE, attrs));

					// Add a capability specific to the workspace so that we can
					// identify this fact later during resource processing.
					attrs = new Attrs();
					attrs.put(WORKSPACE_NAMESPACE, p.getName());

					rb.addCapability(CapabilityBuilder.createCapReqBuilder(WORKSPACE_NAMESPACE, attrs));

					Resource resource = rb.build();

					resources.add(resource);
				}
			}
		}

		addAll(resources);
	}

	public String toString() {
		return "Workspace";
	}

}
