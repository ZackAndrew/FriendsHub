import { useState } from 'react';
import { meetingsApi } from '../api/meetings';
import type { MeetingResponse } from '../types';
import toast from 'react-hot-toast';
import { format } from 'date-fns';
import { CalendarPlus, Send, Clock, MapPin } from 'lucide-react';

export default function Meetings() {
  const [showForm, setShowForm] = useState(false);
  const [participantUsername, setParticipantUsername] = useState('');
  const [title, setTitle] = useState('');
  const [startTime, setStartTime] = useState('');
  const [endTime, setEndTime] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [lastMeeting, setLastMeeting] = useState<MeetingResponse | null>(null);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!participantUsername || !title || !startTime || !endTime) {
      toast.error('Please fill in all fields');
      return;
    }

    if (new Date(startTime) >= new Date(endTime)) {
      toast.error('End time must be after start time');
      return;
    }

    setSubmitting(true);
    try {
      const res = await meetingsApi.create({
        participantUsername,
        title,
        startTime: new Date(startTime).toISOString(),
        endTime: new Date(endTime).toISOString(),
      });
      setLastMeeting(res.data);
      toast.success('Meeting request sent!');
      setShowForm(false);
      setParticipantUsername('');
      setTitle('');
      setStartTime('');
      setEndTime('');
    } catch (err: any) {
      toast.error(err.response?.data?.error || 'Failed to create meeting');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="space-y-4 md:space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <h1 className="text-xl md:text-2xl font-bold text-slate-900">Meetings</h1>
        <button
          onClick={() => setShowForm(!showForm)}
          className="flex items-center justify-center gap-2 px-4 py-2.5 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors text-sm md:text-base"
        >
          <CalendarPlus size={18} />
          New Meeting
        </button>
      </div>

      {/* Create Meeting Form */}
      {showForm && (
        <form onSubmit={handleCreate} className="bg-white rounded-xl border border-slate-200 p-4 md:p-6 space-y-4">
          <h3 className="font-semibold text-sm md:text-base text-slate-900">Create Meeting Request</h3>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 md:gap-4">
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">
                Participant Username
              </label>
              <input
                type="text"
                value={participantUsername}
                onChange={(e) => setParticipantUsername(e.target.value)}
                placeholder="friend_username"
                className="w-full px-3 md:px-4 py-2.5 md:py-3 border border-slate-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none text-sm"
                required
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Meeting Title</label>
              <input
                type="text"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder="Coffee at Starbucks"
                className="w-full px-3 md:px-4 py-2.5 md:py-3 border border-slate-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none text-sm"
                required
              />
            </div>
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
                  <Send size={16} />
                  Send Request
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

      {/* Last Created Meeting */}
      {lastMeeting && (
        <div className="bg-white rounded-xl border border-emerald-200 p-4 md:p-6">
          <h3 className="font-semibold text-emerald-700 mb-3 md:mb-4 flex items-center gap-2 text-sm md:text-base">
            <CalendarPlus size={18} className="md:w-5 md:h-5" />
            Meeting Request Sent!
          </h3>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 md:gap-4">
            <div className="flex items-center gap-3">
              <MapPin size={14} className="text-slate-400 shrink-0 md:w-4 md:h-4" />
              <div>
                <p className="text-xs text-slate-400">Title</p>
                <p className="font-medium text-sm md:text-base text-slate-900">{lastMeeting.title}</p>
              </div>
            </div>
            <div className="flex items-center gap-3">
              <Clock size={14} className="text-slate-400 shrink-0 md:w-4 md:h-4" />
              <div>
                <p className="text-xs text-slate-400">Time</p>
                <p className="font-medium text-sm md:text-base text-slate-900">
                  {format(new Date(lastMeeting.startTime), 'MMM d, HH:mm')} –{' '}
                  {format(new Date(lastMeeting.endTime), 'HH:mm')}
                </p>
              </div>
            </div>
          </div>
          <div className="mt-3 md:mt-4 pt-3 md:pt-4 border-t border-slate-200">
            <p className="text-xs md:text-sm text-slate-500">
              With <span className="font-medium">{lastMeeting.participantUsername}</span>
              {' · '}
              Status:{' '}
              <span className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-amber-100 text-amber-700">
                {lastMeeting.status}
              </span>
            </p>
          </div>
        </div>
      )}

      {/* Info */}
      <div className="bg-slate-50 rounded-xl p-4 md:p-6 text-center">
        <CalendarPlus size={40} className="mx-auto text-slate-300 mb-3 md:w-12 md:h-12" />
        <p className="text-sm md:text-base text-slate-500">Create a meeting request with a friend</p>
        <p className="text-xs md:text-sm text-slate-400 mt-1">
          Both of you need to have overlapping availability for the meeting time
        </p>
      </div>
    </div>
  );
}
