/*******************************************************************************
 * Copyright (c) 2017 Skymatic UG (haftungsbeschränkt).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the accompanying LICENSE file.
 *******************************************************************************/
package org.cryptomator.ui.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.cryptomator.cryptolib.Cryptors;
import org.cryptomator.cryptolib.api.Cryptor;
import org.cryptomator.ui.settings.Localization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;

@Singleton
class UpgradeVersion3DropBundleExtension extends UpgradeStrategy {

	private static final Logger LOG = LoggerFactory.getLogger(UpgradeVersion3DropBundleExtension.class);

	@Inject
	public UpgradeVersion3DropBundleExtension(Localization localization) {
		super(Cryptors.version1(UpgradeStrategy.strongSecureRandom()), localization, 3, 3);
	}

	@Override
	public String getTitle(Vault vault) {
		return localization.getString("upgrade.version3dropBundleExtension.title");
	}

	@Override
	public String getMessage(Vault vault) {
		String fmt = localization.getString("upgrade.version3dropBundleExtension.msg");
		Path path = vault.getPath();
		String oldVaultName = path.getFileName().toString();
		String newVaultName = StringUtils.removeEnd(oldVaultName, ".cryptomator");
		return String.format(fmt, oldVaultName, newVaultName);
	}

	@Override
	protected void upgrade(Vault vault, Cryptor cryptor) throws UpgradeFailedException {
		Path path = vault.getPath();
		String oldVaultName = path.getFileName().toString();
		String newVaultName = StringUtils.removeEnd(oldVaultName, ".cryptomator");
		Path newPath = path.resolveSibling(newVaultName);
		if (Files.exists(newPath)) {
			String fmt = localization.getString("upgrade.version3dropBundleExtension.err.alreadyExists");
			String msg = String.format(fmt, newPath);
			throw new UpgradeFailedException(msg);
		} else {
			try {
				LOG.info("Renaming {} to {}", path, newPath.getFileName());
				Files.move(path, path.resolveSibling(newVaultName));
				Platform.runLater(() -> {
					vault.getVaultSettings().path().set(newPath);
				});
			} catch (IOException e) {
				LOG.error("Vault migration failed", e);
				throw new UpgradeFailedException();
			}
		}
	}

	@Override
	public boolean isApplicable(Vault vault) {
		Path vaultPath = vault.getPath();
		if (vaultPath.toString().endsWith(".cryptomator")) {
			return super.isApplicable(vault);
		} else {
			return false;
		}
	}

}
