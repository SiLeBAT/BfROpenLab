/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
package org.apache.poi.poifs.crypt.agile;

import java.io.ByteArrayInputStream;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.poifs.crypt.ChainingMode;
import org.apache.poi.poifs.crypt.CipherAlgorithm;
import org.apache.poi.poifs.crypt.EncryptionVerifier;
import org.apache.poi.poifs.crypt.HashAlgorithm;

import com.microsoft.schemas.office.x2006.encryption.CTKeyEncryptor;
import com.microsoft.schemas.office.x2006.encryption.EncryptionDocument;
import com.microsoft.schemas.office.x2006.encryption.STCipherChaining;
import com.microsoft.schemas.office.x2006.keyEncryptor.certificate.CTCertificateKeyEncryptor;
import com.microsoft.schemas.office.x2006.keyEncryptor.password.CTPasswordKeyEncryptor;

/**
 * Used when checking if a key is valid for a document 
 */
public class AgileEncryptionVerifier extends EncryptionVerifier {

    public static class AgileCertificateEntry {
        X509Certificate x509;
        byte encryptedKey[];
        byte certVerifier[];
    }
    
    private List<AgileCertificateEntry> certList = new ArrayList<AgileCertificateEntry>();

    public AgileEncryptionVerifier(String descriptor) {
        this(AgileEncryptionInfoBuilder.parseDescriptor(descriptor));
    }
    
    protected AgileEncryptionVerifier(EncryptionDocument ed) {
        Iterator<CTKeyEncryptor> encList = ed.getEncryption().getKeyEncryptors().getKeyEncryptorList().iterator();
        CTPasswordKeyEncryptor keyData;
        try {
            keyData = encList.next().getEncryptedPasswordKey();
            if (keyData == null) {
                throw new NullPointerException("encryptedKey not set");
            }
        } catch (Exception e) {
            throw new EncryptedDocumentException("Unable to parse keyData", e);
        }
        
        int keyBits = (int)keyData.getKeyBits();
        
        CipherAlgorithm ca = CipherAlgorithm.fromXmlId(keyData.getCipherAlgorithm().toString(), keyBits);
        setCipherAlgorithm(ca);

        int hashSize = keyData.getHashSize();

        HashAlgorithm ha = HashAlgorithm.fromEcmaId(keyData.getHashAlgorithm().toString());
        setHashAlgorithm(ha);

        if (getHashAlgorithm().hashSize != hashSize) {
            throw new EncryptedDocumentException("Unsupported hash algorithm: " + 
                    keyData.getHashAlgorithm() + " @ " + hashSize + " bytes");
        }

        setSpinCount(keyData.getSpinCount());
        setEncryptedVerifier(keyData.getEncryptedVerifierHashInput());
        setSalt(keyData.getSaltValue());
        setEncryptedKey(keyData.getEncryptedKeyValue()); 
        setEncryptedVerifierHash(keyData.getEncryptedVerifierHashValue());

        int saltSize = keyData.getSaltSize();
        if (saltSize != getSalt().length)
            throw new EncryptedDocumentException("Invalid salt size");
        
        switch (keyData.getCipherChaining().intValue()) {
            case STCipherChaining.INT_CHAINING_MODE_CBC:
                setChainingMode(ChainingMode.cbc);
                break;
            case STCipherChaining.INT_CHAINING_MODE_CFB:
                setChainingMode(ChainingMode.cfb);
                break;
            default:
                throw new EncryptedDocumentException("Unsupported chaining mode - "+keyData.getCipherChaining().toString());
        }
        
        if (!encList.hasNext()) return;
        
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            while (encList.hasNext()) {
                CTCertificateKeyEncryptor certKey = encList.next().getEncryptedCertificateKey();
                AgileCertificateEntry ace = new AgileCertificateEntry();
                ace.certVerifier = certKey.getCertVerifier();
                ace.encryptedKey = certKey.getEncryptedKeyValue();
                ace.x509 = (X509Certificate)cf.generateCertificate(new ByteArrayInputStream(certKey.getX509Certificate()));
                certList.add(ace);
            }
        } catch (GeneralSecurityException e) {
            throw new EncryptedDocumentException("can't parse X509 certificate", e);
        }
    }
    
    public AgileEncryptionVerifier(CipherAlgorithm cipherAlgorithm, HashAlgorithm hashAlgorithm, int keyBits, int blockSize, ChainingMode chainingMode) {
        setCipherAlgorithm(cipherAlgorithm);
        setHashAlgorithm(hashAlgorithm);
        setChainingMode(chainingMode);
        setSpinCount(100000); // TODO: use parameter
    }
    
    protected void setSalt(byte salt[]) {
        if (salt == null || salt.length != getCipherAlgorithm().blockSize) {
            throw new EncryptedDocumentException("invalid verifier salt");
        }
        super.setSalt(salt);
    }
    
    // make method visible for this package
    protected void setEncryptedVerifier(byte encryptedVerifier[]) {
        super.setEncryptedVerifier(encryptedVerifier);
    }

    // make method visible for this package
    protected void setEncryptedVerifierHash(byte encryptedVerifierHash[]) {
        super.setEncryptedVerifierHash(encryptedVerifierHash);
    }

    // make method visible for this package
    protected void setEncryptedKey(byte[] encryptedKey) {
        super.setEncryptedKey(encryptedKey);
    }
    
    public void addCertificate(X509Certificate x509) {
        AgileCertificateEntry ace = new AgileCertificateEntry();
        ace.x509 = x509;
        certList.add(ace);
    }
    
    public List<AgileCertificateEntry> getCertificates() {
        return certList;
    }
}
