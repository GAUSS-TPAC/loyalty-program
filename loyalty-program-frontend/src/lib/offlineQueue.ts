import { eventsApi, type IncomingEventRequest } from "@/lib/api";

const STORAGE_KEY = "loyalty_offline_event_queue";

export interface QueuedEvent {
  id: string;
  payload: IncomingEventRequest;
  idempotencyKey: string;
  queuedAt: string;
}

function generateId() {
  return `idem-${Date.now()}-${Math.random().toString(36).substring(2, 8)}`;
}

export function getQueue(): QueuedEvent[] {
  if (typeof window === "undefined") return [];
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? (JSON.parse(raw) as QueuedEvent[]) : [];
  } catch {
    return [];
  }
}

function saveQueue(queue: QueuedEvent[]) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(queue));
}

export function enqueue(payload: IncomingEventRequest, idempotencyKey?: string): QueuedEvent {
  const entry: QueuedEvent = {
    id: generateId(),
    payload,
    idempotencyKey: idempotencyKey || generateId(),
    queuedAt: new Date().toISOString(),
  };
  saveQueue([...getQueue(), entry]);
  return entry;
}

export function removeFromQueue(id: string) {
  saveQueue(getQueue().filter((item) => item.id !== id));
}

/**
 * Replays queued events sequentially (preserves submission order). Stops on the
 * first network failure so remaining items stay queued for the next trigger;
 * a genuine application error (e.g. 400) is dropped rather than retried forever.
 */
export async function flushQueue(): Promise<{ synced: number; remaining: number }> {
  const queue = getQueue();
  let synced = 0;

  for (const item of queue) {
    try {
      await eventsApi.processEvent(item.payload, item.idempotencyKey);
      removeFromQueue(item.id);
      synced++;
    } catch (err) {
      if (!navigator.onLine || err instanceof TypeError) {
        break;
      }
      removeFromQueue(item.id);
    }
  }

  return { synced, remaining: getQueue().length };
}
