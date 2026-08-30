#!/bin/bash
echo "Run this script locally to generate a keystore and the necessary GitHub Secrets commands."
echo ""
keytool -genkey -v -keystore release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias rpdev -dname "CN=RPDevs, OU=Builds, O=RPDevs, L=Unknown, S=Unknown, C=US" -storepass rpdevs123 -keypass rpdevs123
echo ""
echo "Now, go to your GitHub Repo -> Settings -> Secrets and variables -> Actions, and add the following secrets:"
echo ""
echo "1. SIGNING_KEY: (Copy the output of this command)"
base64 -w 0 release.jks
echo ""
echo ""
echo "2. ALIAS: rpdev"
echo "3. KEY_STORE_PASSWORD: rpdevs123"
echo "4. KEY_PASSWORD: rpdevs123"
