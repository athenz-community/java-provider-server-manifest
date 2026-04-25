import fs from "fs";
import https from "https";

const ATHENZ_CONF = {
  zts: {
    hostname: "localhost",
    port: 8443,
  },
  certPath: "../../athenz_dist/certs/athenz_admin.cert.pem",
  keyPath: "../../athenz_dist/keys/athenz_admin.private.pem",
  scope: "api:role.docs-getter api:role.docs-poster",
};

let cachedToken = null;
let expiryTime = 0;

export async function getAthenzToken() {
  const now = Date.now();
  
  if (cachedToken && expiryTime > now + 60000) {
    return cachedToken;
  }

  console.error("[Athenz] Fetching new access token...");

  return new Promise((resolve, reject) => {
    const cert = fs.readFileSync(ATHENZ_CONF.certPath);
    const key = fs.readFileSync(ATHENZ_CONF.keyPath);

    const options = {
      hostname: ATHENZ_CONF.zts.hostname,
      port: ATHENZ_CONF.zts.port,
      path: `/zts/v1/oauth2/token?grant_type=client_credentials&scope=${ATHENZ_CONF.scope}`,
      method: "POST",
      key: key,
      cert: cert,
      rejectUnauthorized: false,
    };

    const req = https.request(options, (res) => {
      let data = "";
      res.on("data", (chunk) => (data += chunk));
      res.on("end", () => {
        if (res.statusCode === 200) {
          const result = JSON.parse(data);
          cachedToken = result.access_token;
          expiryTime = now + (result.expires_in * 1000);
          resolve(cachedToken);
        } else {
          reject(new Error(`Athenz auth failed: ${res.statusCode} ${data}`));
        }
      });
    });

    req.on("error", reject);
    req.end();
  });
}
