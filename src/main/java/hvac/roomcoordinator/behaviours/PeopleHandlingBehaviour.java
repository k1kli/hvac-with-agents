package hvac.roomcoordinator.behaviours;

import hvac.ontologies.meeting.Meeting;
import hvac.ontologies.presence.*;
import hvac.roomcoordinator.RoomContext;
import hvac.time.DateTimeSimulator;
import hvac.util.behaviours.RequestProcessingBehaviour;
import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static hvac.util.SimpleReplies.*;

public class PeopleHandlingBehaviour extends RequestProcessingBehaviour {
    private final RoomContext context;
    private final UpkeeperManagingBehaviour upkeeperManagingBehaviour;
    private final MessageTemplate template = MessageTemplate.and(
            MessageTemplate.MatchLanguage(FIPANames.ContentLanguage.FIPA_SL0),
            MessageTemplate.MatchOntology(PresenceOntology.getInstance().getName())
    );
    private final List<Presence> presences = new ArrayList<>();

    public PeopleHandlingBehaviour(Agent agent, RoomContext context, UpkeeperManagingBehaviour upkeeperManagingBehaviour) {
        super(agent, context.getLogger());
        this.context = context;
        this.upkeeperManagingBehaviour = upkeeperManagingBehaviour;
    }

    @Override
    protected void processRequest(ACLMessage msg) throws Codec.CodecException, OntologyException {
        removeFinishedPresences();
        boolean handled = false;
        ContentElement ce = myAgent.getContentManager().extractContent(msg);
        if (ce instanceof Action) {
            Concept action = ((Action) ce).getAction();
            if (action instanceof RequestCurrentPresences) {
                handleRequestCurrentPresences(msg);
                handled = true;
            } else if (action instanceof RequestAddPresence) {
                RequestAddPresence requestAddPresence = (RequestAddPresence) action;
                handleRequestAddPresence(msg, requestAddPresence);
                handled = true;
            }
        }
        reconstructMeetings();
        upkeeperManagingBehaviour.reset(1);
        if(!handled) replyNotUnderstood(myAgent, msg);
    }

    private void removeFinishedPresences() {
        presences.removeIf(presence ->
                presence.getUntilLocal() != null
                        && presence.getUntilLocal().isBefore(DateTimeSimulator.getCurrentDate()));
    }

    private void handleRequestCurrentPresences(ACLMessage msg) throws Codec.CodecException, OntologyException {
        PresencesInfo info = new PresencesInfo(presences);
        ACLMessage reply = msg.createReply();
        reply.setPerformative(ACLMessage.INFORM);
        myAgent.getContentManager().fillContent(reply, info);
        myAgent.send(reply);
        context.getLogger().log("replied with presence info to " + msg.getSender());
    }

    private void handleRequestAddPresence(ACLMessage msg, RequestAddPresence requestAddPresence) {
        Presence presenceToAdd = requestAddPresence.getPresence();
        //if presence is incorrect or not for my room stop handling
        if(!presenceToAdd.isValid() || presenceToAdd.getRoomId() != context.getMyRoomId()) {
            context.getLogger().log("invalid presence to add " + msg.getContent());
            replyRefuse(myAgent, msg);
        }
        //if presence only indicates end time search for matching start time and update that presence with end time
        else if(presenceToAdd.getSinceLocal() == null) {
            for(int i = presences.size()-1; i>= 0; i--) {
                Presence presence = presences.get(i);
                if(presence.getPerson().equals(presenceToAdd.getPerson())
                    && presence.getUntilLocal() == null) {
                    context.getLogger().log("set leave time for " + msg.getSender());
                    presence.setUntilLocal(presenceToAdd.getUntilLocal());
                    replyAgree(myAgent, msg);
                    return;
                }
            }
            context.getLogger().log("no matching enter time for given leave time for person " + msg.getSender());
            replyRefuse(myAgent, msg);
            //if presence is not end time and for person already in presences stop handling
        } else if(presences.stream().anyMatch(presence->presence.getPerson().equals(presenceToAdd.getPerson()))) {
            context.getLogger().log("can't add presence for a person (this person already added their presence) " + msg.getSender());
            replyRefuse(myAgent, msg);
        }
        //if presence is first presence to add or ends before first presence starts then we can just add it
        else if (presences.size() == 0
                || presenceToAdd.getUntilLocal() != null
                && presences.get(0).getSinceLocal().isAfter(presenceToAdd.getUntilLocal())) {
            presences.add(0, presenceToAdd);
            replyAgree(myAgent, msg);
        } else {
            List<Presence> newPresences = new ArrayList<>(presences);
            int i = 0;
            for (; i < newPresences.size(); i++) {
                if (newPresences.get(i).getSinceLocal().isAfter(presenceToAdd.getSinceLocal())) {
                    break;
                }
            }
            newPresences.add(i, presenceToAdd);
            if (arePresencesValid(newPresences)) {
                context.getLogger().log("found place for " + msg.getSender());
                presences.clear();
                presences.addAll(newPresences);
                replyAgree(myAgent, msg);
            } else {
                context.getLogger().log("failed to find place for " + msg.getSender());
                replyRefuse(myAgent, msg);
            }
        }
    }

    //checks if at any point there will be too many people in the room
    private boolean arePresencesValid(List<Presence> presenceList) {
        int maxPresences = context.getSeats();
        LinkedList<Presence> concurrentPresences = new LinkedList<>();
        for (Presence presence :
                presenceList) {
            concurrentPresences.removeIf(it->it.getUntilLocal() != null &&
                    (it.getUntilLocal().isBefore(presence.getSinceLocal())
            || it.getUntilLocal().isEqual(presence.getSinceLocal())));
            concurrentPresences.add(presence);
            if (concurrentPresences.size() > maxPresences) {
                return false;
            }
        }
        return true;
    }

    private void reconstructMeetings() {
        if(presences.isEmpty()) context.setMeetings(Collections.emptyList());
        //list of all points in time in which set of people present could change
        List<LocalDateTime> presenceChangeTimes = Stream.concat(
                presences.stream().map(Presence::getSinceLocal),
                presences.stream().map(Presence::getUntilLocal).filter(Objects::nonNull)
        ).distinct().collect(Collectors.toList());
        //at each index set of people that are in the room from time presenceChangeTimes[i]
        //to presenceChangeTimes[i+1] (or till infinity in case of last element)
        List<List<AID>> peopleInRoomSinceChangeTime = presenceChangeTimes.stream()
                .map(dt->new ArrayList<AID>()).collect(Collectors.toList());

        int presenceChangeTimeBeginIndex = 0;
        for(Presence presence : presences) {
            //skip all timeslots which for sure won't have more people
            while(presenceChangeTimes.get(presenceChangeTimeBeginIndex).isBefore(presence.getSinceLocal())) {
                presenceChangeTimeBeginIndex++;
            }
            //add person to all timeslots which are part of presence
            for(int presenceChangeTimeIndex = presenceChangeTimeBeginIndex;
                presenceChangeTimeIndex < presenceChangeTimes.size()
                        && (presence.getUntilLocal() == null
            || presenceChangeTimes.get(presenceChangeTimeIndex).isBefore(presence.getUntilLocal()));
                presenceChangeTimeIndex++) {
                peopleInRoomSinceChangeTime.get(presenceChangeTimeIndex).add(presence.getPerson());
            }
        }
        List<Meeting> newMeetings = new ArrayList<>();
        //todo calculate temperature to maintain from db
        for(int i = 0; i < peopleInRoomSinceChangeTime.size()-1; i++) {
            if(!peopleInRoomSinceChangeTime.get(peopleInRoomSinceChangeTime.size()-1).isEmpty()) {
                newMeetings.add(new Meeting(
                        "",
                        presenceChangeTimes.get(i),
                        presenceChangeTimes.get(i + 1),
                        peopleInRoomSinceChangeTime.get(i).size(),
                        273 + 21));
            }
        }
        if(!peopleInRoomSinceChangeTime.get(presenceChangeTimes.size()-1).isEmpty()) {
            newMeetings.add(new Meeting("",
                    presenceChangeTimes.get(presenceChangeTimes.size()-1),
                    //LocalDateTime.MAX here causes log overflow in jade message creation
                    presenceChangeTimes.get(presenceChangeTimes.size()-1).plusYears(5),
                    peopleInRoomSinceChangeTime.get(presenceChangeTimes.size()-1).size(),
                    273+21));
        }
        context.setMeetings(newMeetings);
    }

    @Override
    public MessageTemplate getTemplate() {
        return template;
    }

}
