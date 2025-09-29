import { d as defineEventHandler, a as readBody, c as createError } from '../../../nitro/nitro.mjs';
import { MongoClient } from 'mongodb';
import 'node:http';
import 'node:https';
import 'node:events';
import 'node:buffer';
import 'node:fs';
import 'node:path';
import 'node:crypto';
import 'node:url';

const mongoUri = process.env.MONGO_URI || "mongodb://localhost:27017";
const client = new MongoClient(mongoUri);
const _id__put = defineEventHandler(async (event) => {
  const providerId = event.context.params.id;
  const body = await readBody(event);
  try {
    await client.connect();
    const database = client.db("flow-service");
    const providersCollection = database.collection("providers");
    const { apiKey, baseUrl, selectedModel, models, active } = body;
    const encryptedApiKey = `encrypted(${apiKey})`;
    const result = await providersCollection.updateOne(
      { _id: providerId },
      {
        $set: {
          apiKey: encryptedApiKey,
          // Save the encrypted key
          baseUrl,
          selectedModel,
          models,
          active,
          updatedAt: /* @__PURE__ */ new Date()
        }
      },
      { upsert: true }
      // This will create the document if it doesn't exist
    );
    console.log(
      `MongoDB: Processed provider [${providerId}]. Matched: ${result.matchedCount}, Modified: ${result.modifiedCount}, Upserted: ${result.upsertedCount}`
    );
    return {
      id: providerId,
      ...body,
      apiKey: "********"
      // Mask the key in the response
    };
  } catch (error) {
    console.error("Error updating provider in MongoDB:", error);
    throw createError({
      statusCode: 500,
      statusMessage: "Failed to update provider configuration."
    });
  } finally {
    await client.close();
  }
});

export { _id__put as default };
//# sourceMappingURL=_id_.put.mjs.map
