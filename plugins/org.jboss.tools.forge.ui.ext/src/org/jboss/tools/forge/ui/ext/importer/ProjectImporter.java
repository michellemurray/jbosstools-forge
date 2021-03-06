package org.jboss.tools.forge.ui.ext.importer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.maven.model.Model;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IProjectConfigurationManager;
import org.eclipse.m2e.core.project.MavenProjectInfo;
import org.eclipse.m2e.core.project.ProjectImportConfiguration;

/**
 * Imports a maven-ized project into the workspace
 * 
 * FOR INTERNAL USE ONLY. This class was copied from the
 * org.jboss.tools.forge.ui plugin in order to avoid dependency on it and should
 * be removed in future versions.
 * 
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 * 
 */
class ProjectImporter {

	private String baseDirPath;
	private String moduleLocation;
	private String projectName;

	public ProjectImporter(String baseDirPath, String moduleLocation,
			String projectName) {
		this.baseDirPath = baseDirPath;
		this.moduleLocation = moduleLocation;
		this.projectName = projectName;
	}

	public void importProject() {
		Job job = new MavenImportWorkspaceJob("Importing project "
				+ projectName);
		job.schedule();
	}

	private Collection<MavenProjectInfo> getProjectToImport() {
		ArrayList<MavenProjectInfo> result = new ArrayList<MavenProjectInfo>(1);
		result.add(createMavenProjectInfo());
		return result;
	}

	private MavenProjectInfo createMavenProjectInfo() {
		MavenProjectInfo result = null;
		try {
			File projectDir = new File(baseDirPath, moduleLocation);
			File pomFile = new File(projectDir, "pom.xml");
			Model model = MavenPlugin.getMavenModelManager().readMavenModel(
					pomFile);
			String pomName = moduleLocation + "/" + "pom.xml";
			result = new MavenProjectInfo(pomName, pomFile, model, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return result;
	}

	private class MavenImportWorkspaceJob extends WorkspaceJob {

		public MavenImportWorkspaceJob(String name) {
			super(name);
			setRule(MavenPlugin.getProjectConfigurationManager().getRule());
		}

		@Override
		public IStatus runInWorkspace(IProgressMonitor monitor) {
			try {
				ProjectImportConfiguration config = new ProjectImportConfiguration();
				IProjectConfigurationManager configManager = MavenPlugin
						.getProjectConfigurationManager();
				Collection<MavenProjectInfo> projectToImport = getProjectToImport();
				configManager.importProjects(projectToImport, config, monitor);
			} catch (CoreException ex) {
				return ex.getStatus();
			}
			return Status.OK_STATUS;
		}
	}

}