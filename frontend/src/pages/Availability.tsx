import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { availabilityApi } from '../api/availability';
import type { AvailabilitySlot } from '../types';
import toast from 'react-hot-toast';
import { format } from 'date-fns';
import { Calendar, Plus, Clock } from 'lucide-react';

export default function Availability() {
  const { user } = useAuth();
  const [slots, setSlots] = useState<AvailabilitySlot[]>([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [startTime, setStartTime] = useState('');
  const [endTime, setEndTime] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const loadSlots = async () => {
    if (!user) return;
    try {
      const res = await availabilityApi.getUserSlots(user.id);
      setSlots(res.data);
    } catch (err) {
      toast.error('Failed to load availability');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadSlots();
  }, [user]);

  const handleAdd = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!startTime || !endTime) {
      toast.error('Please fill in both start and end time');
      return;
    }

    if (new Date(startTime) >= new Date(endTime)) {
      toast.error('End time must be after start time');
      return;
    }

    setSubmitting(true);
    try {
      await availabilityApi.addSlot({
        startTime: new Date(startTime).toISOString(),
        endTime: new Date(endTime).toISOString(),
      });
      toast.success('Availability slot added!');
      setShowForm(false);
      setStartTime('');
      setEndTime('');
      loadSlots();
    } catch (err: any) {
      toast.error(err.response?.data?.error || 'Failed to add slot');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  return (
    <div className="space-y-4 md:space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <h1 className="text-xl md:text-2xl font-bold text-slate-900">My Schedule</h1>
        <button
          onClick={() => setShowForm(!showForm)}
          className="flex items-center justify-center gap-2 px-4 py-2.5 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors text-sm md:text-base"
        >
          <Plus size={18} />
          Add Slot
        </button>
      </div>

      {/* Add Slot Form */}
      {showForm && (
        <form onSubmit={handleAdd} className="bg-white rounded-xl border border-slate-200 p-4 md:p-6 space-y-4">
          <h3 className="font-semibold text-sm md:text-base text-slate-900">New Availability Slot</h3>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 md:gap-4">
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Start Time</label>
              <input
                type="datetime-local"
                value={startTime}
                onChange={(e) => setStartTime(e.target.value)}
                className="w-full px-3 md:px-4 py-2.5 md:py-3 border border-slate-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none text-sm"
                required
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">End Time</label>
              <input
                type="datetime-local"
                value={endTime}
                onChange={(e) => setEndTime(e.target.value)}
                className="w-full px-3 md:px-4 py-2.5 md:py-3 border border-slate-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none text-sm"
                required
              />
            </div>
          </div>
          <div className="flex gap-2">
            <button
              type="submit"
              disabled={submitting}
              className="flex items-center gap-2 px-4 md:px-6 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 disabled:opacity-50 transition-colors text-sm"
            >
              {submitting ? (
                <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
              ) : (
                <>
                  <Plus size={16} />
                  Save Slot
                </>
              )}
            </button>
            <button
              type="button"
              onClick={() => setShowForm(false)}
              className="px-4 md:px-6 py-2 bg-slate-200 text-slate-700 rounded-lg hover:bg-slate-300 transition-colors text-sm"
            >
              Cancel
            </button>
          </div>
        </form>
      )}

      {/* Slots List */}
      <div className="grid gap-2 md:gap-3">
        {slots.length === 0 ? (
          <div className="text-center py-8 md:py-12 bg-white rounded-xl border border-slate-200">
            <Calendar size={40} className="mx-auto text-slate-300 mb-3 md:w-12 md:h-12" />
            <p className="text-sm md:text-base text-slate-500">No availability slots yet</p>
            <p className="text-xs md:text-sm text-slate-400 mt-1">Add your free time so friends can find you!</p>
          </div>
        ) : (
          slots.map((slot) => (
            <div
              key={slot.id}
              className="bg-white rounded-xl border border-slate-200 p-3 md:p-4 flex items-center gap-3 md:gap-4"
            >
              <div className="w-8 h-8 md:w-10 md:h-10 bg-emerald-100 text-emerald-600 rounded-full flex items-center justify-center shrink-0">
                <Clock size={16} className="md:w-[18px] md:h-[18px]" />
              </div>
              <div>
                <p className="font-medium text-sm md:text-base text-slate-900">
                  {format(new Date(slot.startTime), 'EEEE, MMM d, yyyy')}
                </p>
                <p className="text-xs md:text-sm text-slate-500">
                  {format(new Date(slot.startTime), 'HH:mm')} –{' '}
                  {format(new Date(slot.endTime), 'HH:mm')}
                </p>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}
