package model.signing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.pdmodel.PDDocument;

import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.FileDocument;
import eu.europa.esig.dss.model.SignatureValue;
import eu.europa.esig.dss.model.ToBeSigned;
import eu.europa.esig.dss.pades.PAdESSignatureParameters;
import eu.europa.esig.dss.pades.signature.PAdESService;
import eu.europa.esig.dss.token.SignatureTokenConnection;
import model.certificates.Certificate;
import model.certificates.CertificatesHolder;
import model.signing.visible.SignatureAspect;

/*
 * ia totul de-a gata configurat si semneaza
 */

public class PDFVisibleSigning implements SigningMode {// class asta se instantiaza o singura data in momentul in care intru pe semnare pdf si semnez ceva, nu si la semnare multi/semnare intre documente.
	
	private CertificatesHolder certificatesHolder;
	private PAdESService service;
	private PAdESSignatureParameters padesParameters;
	private SignatureAspect signatureAspect;

	
	public PDFVisibleSigning(CertificatesHolder certificatesHolder, PAdESService service, PAdESSignatureParameters padesParameters, SignatureAspect signatureAspect) {
		this.certificatesHolder = certificatesHolder;
		this.service = service;
		this.padesParameters = padesParameters;
		this.signatureAspect = signatureAspect;
	}
	
	@Override
	public void performSign(File file) throws FileNotFoundException, IOException { // aici as putea cel mult sa calculez pozitia semnaturii
		System.out.println("Signing with visible signature");
		InputStream resource = new FileInputStream(file);
		PDDocument pdDocument = PDDocument.load(resource);
		SignatureTokenConnection token = this.certificatesHolder.getToken();
		Certificate cert = this.certificatesHolder.getSelectedCertificate();
		
		if (pdDocument.getNumberOfPages() < this.signatureAspect.getPage())
			this.signatureAspect.setPage(pdDocument.getNumberOfPages());
		System.out.println(this.signatureAspect.getPage());
		FileDocument toSignDocument = new FileDocument(file);
		padesParameters.setImageParameters(signatureAspect.getSIP());
		ToBeSigned dataToSign = service.getDataToSign(toSignDocument, padesParameters);
		SignatureValue signatureValue = token.sign(dataToSign, padesParameters.getDigestAlgorithm(), cert.getPrivateKey());
		DSSDocument signedDocument = service.signDocument(toSignDocument, padesParameters, signatureValue);
		signedDocument.save(file.getAbsolutePath() + "-semnat.pdf");
	}
}
