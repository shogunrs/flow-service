// This file is located at: /server/api/providers/[id].put.ts
// It handles PUT requests to /api/providers/openai, /api/providers/gemini, etc.

// You will need to install a MongoDB driver: `npm install mongodb`
import { MongoClient } from "mongodb";

// IMPORTANT: Use environment variables for sensitive data.
// Replace with your actual MongoDB connection string.
const mongoUri = process.env.MONGO_URI || "mongodb://localhost:27017";
const client = new MongoClient(mongoUri);

export default defineEventHandler(async (event) => {
  // The [id] from the filename becomes a parameter, e.g., 'openai'
  const providerId = event.context.params.id;
  const body = await readBody(event);

  try {
    await client.connect();
    const database = client.db("flow-service"); // <-- IMPORTANT: Replace with your DB name
    const providersCollection = database.collection("providers");

    const { apiKey, baseUrl, selectedModel, models, active } = body;

    // --- SECURITY WARNING ---
    // In a real-world application, you MUST encrypt the apiKey before saving it to the database.
    // This is a placeholder for your encryption logic.
    const encryptedApiKey = `encrypted(${apiKey})`;

    const result = await providersCollection.updateOne(
      { _id: providerId },
      {
        $set: {
          apiKey: encryptedApiKey, // Save the encrypted key
          baseUrl,
          selectedModel,
          models,
          active,
          updatedAt: new Date(),
        },
      },
      { upsert: true } // This will create the document if it doesn't exist
    );

    console.log(
      `MongoDB: Processed provider [${providerId}]. Matched: ${result.matchedCount}, Modified: ${result.modifiedCount}, Upserted: ${result.upsertedCount}`
    );

    // Return the updated data, but mask the API key for security.
    return {
      id: providerId,
      ...body,
      apiKey: "********", // Mask the key in the response
    };
  } catch (error) {
    console.error("Error updating provider in MongoDB:", error);
    // Return a structured error response
    throw createError({
      statusCode: 500,
      statusMessage: "Failed to update provider configuration.",
    });
  } finally {
    // Ensure the client is closed in both success and error cases
    await client.close();
  }
});
